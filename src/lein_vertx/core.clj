(ns lein-vertx.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [leiningen.core.classpath :as classpath]
            [leiningen.core.project :as project]
            [leiningen.core.eval :as eval]
            [leiningen.core.user :as user]
            [leiningen.core.main :refer [debug]]
            [clojure.data.json :as json])
  (:import [java.io FileOutputStream BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]
           [java.util.regex Pattern]))

(defn ^:internal home-dir
  "Returns the home-dir for the plugin, creating if necessary.
   The home-dir defaults to ~/.lein/lein-vertx, and is used to store
   downloaded modules and vertx config."
  []
  (let [dir (io/file (user/leiningen-home) "lein-vertx")
        conf-dir (io/file dir "conf")]
    (when-not (.exists dir)
      (println "Creating lein-vertx conf dir at" (.getAbsolutePath dir))
      (.mkdirs conf-dir)
      (doseq [n ["langs.properties" "repos.txt" "logging.properties"]]
        (io/copy (io/reader (io/resource (str "lein-vertx/_" n)))
          (io/file conf-dir n))))
    dir))

(defn ^:internal mods-dir
  "Returns the vertx mods dir inside home-dir."
  []
  (io/file (home-dir) "mods"))

(defn ^:internal conf-dir
  "Returns the vertx conf dir inside home-dir."
  []
  (io/file (home-dir) "conf"))

(defn ^:internal conf-file-path
  "Looks up the path for the given conf file.
   TODO: support looking at the project's classpath first, to allow
  the file to be overridden on a per-project basis."
  [f]
  (.getAbsolutePath (io/file (conf-dir) f)))

(def ^:internal vertx-deps-project
  "Dummy project used to resolve vertx deps for the classpath of the subprocess"
  {:dependencies '[[io.vertx/vertx-platform "2.1M1"]]
   :repositories [["central" {:url "http://repo1.maven.org/maven2/" :snapshots false}]
                  ["sonatype" {:url "http://oss.sonatype.org/content/repositories/snapshots" :snapshots true}]
                  ["bintray" {:url "http://dl.bintray.com"}]]})

(defn ^:internal make-classpath
  "Creates a classpath for the subprocess.
   It consists of:
   * the classpath for the project
   * the plugin conf-dir
   * the vertx jars"
  [project]
  (string/join java.io.File/pathSeparatorChar
            (concat
             ;; exclude the :provided profile, which should be used to
             ;; bring in vertx deps for local dev that we don't want
             ;; on the container classpath
             (classpath/get-classpath (project/unmerge-profiles project [:provided]))
             [(conf-dir)]
             (map (memfn getAbsolutePath)
                  (classpath/resolve-dependencies :dependencies vertx-deps-project)))))

(defn ^:internal sh-command
  "Generates the command for the subprocess."
  [project args]
  (concat [(System/getenv "LEIN_JAVA_CMD")
           (str "-Djava.util.logging.config.file=" (conf-file-path "logging.properties"))
           (str "-Dvertx.mods=" (.getAbsolutePath (mods-dir)))
           "-classpath"
           (make-classpath project)
           "org.vertx.java.platform.impl.cli.Starter"]
          args))

(defn ^:internal synthesize-main
  "Creates a verticle main that requires the ns for fn, and invokes fn."
  [fn]
  (let [ns (symbol (namespace fn))]
    (pr-str `(~'ns ~'verticle-main
               (:require ~ns))
            `(~fn))))

(defn verticlize
  "Convert namespaced function name into a verticle name"
  [main]
  (str (string/join (map string/capitalize (-> main (string/replace "/" ".") (string/split #"\.")))) ".clj"))

(defn ^:internal write-main
  "Writes out a verticle main to the compile-path that will invoke [:vertx :main] from project."
  [project main]
  (let [verticle-name (verticlize main)
        compile-dir (doto (io/file (:compile-path project))
                      .mkdirs)
        verticle-file (io/file compile-dir verticle-name)]
    (spit verticle-file
          (str (synthesize-main main)
               "\n"))
    verticle-name))

(defn ^:internal modjson-path
  [project]
  (str (:compile-path project) "/mod.json"))

(defn ^:internal write-mod-json
  "Write module descriptor file to mod.json in target-path"
  [project verticle]
  (let [modjson (modjson-path project)]
    (with-open [w (io/writer modjson)]
      (json/write (merge {:main verticle}
                         (-> project :vertx (dissoc :main))
                         {:description (:description project)
                          :homepage (:url project)
                          :licenses [(-> project :license :name)]})
                  w
                  :escape-slash false))
    modjson))

(defn libs
  "Resolve dependencies jars of the project."
  [project]
  (classpath/resolve-dependencies :dependencies project))

(defn ^{:internal true} unix-path [path]
  (.replace path "\\" "/"))

(defn ^:internal trim-leading-str
  [s to-trim]
  (.replaceAll s (str "^" (Pattern/quote to-trim)) ""))

(defn entry-points
  "Files to be copied into module's classpath."
  [project root-dir]
  (let [root-path (.getAbsolutePath root-dir)]
    (reduce (fn [acc filespec]
              (let [path (reduce trim-leading-str (unix-path (.getAbsolutePath filespec)) [root-path "/"])]
                (if (not (empty? path))
                  (conj acc {:name path :content filespec})
                  acc)))

            []
            (filter #(.exists %) (file-seq root-dir)))))

(defn write-zip [outfile filespecs]
  (with-open [zipfile (-> outfile
                          (FileOutputStream.)
                          (BufferedOutputStream.)
                          (ZipOutputStream.))]
    (doseq [filespec (:classpath filespecs)]
      (let [root-path (.getAbsolutePath (io/file "."))]
        (if (.isDirectory (:content filespec))
          (.putNextEntry zipfile (ZipEntry. (str (:name filespec) "/")))
          (do
            (.putNextEntry zipfile (ZipEntry. (:name filespec)))
            (io/copy (:content filespec) zipfile)))))
    (.putNextEntry zipfile (ZipEntry. "lib/"))
    (doseq [jar (:libs filespecs)]
      (.putNextEntry zipfile (ZipEntry. (str "lib/" (.getName jar))))
      (io/copy jar zipfile))))

(defn outfile
  [project]
  (let [name (:name project)
        version (:version project)
        target (doto (io/file (:target-path project)) .mkdirs)]
    (str (io/file target (format "%s-%s.zip" name version)))))

(defn potential-entry-points
  [project]
  (cons (:compile-path project) (:source-paths project)))

(defn buildmod
  "Generate a zip file for the vertx module"
  [project main-fn & args]
  (let [verticle-name (write-main project (-> project :vertx :main))]
    (write-mod-json project verticle-name)
    (write-zip (outfile project)
             {:classpath  (flatten (map #(entry-points project (io/file %)) (potential-entry-points project)))
              :libs (libs project)})))

(defn invoke-vertx
  "Invokes vertx in the given project."
  [project & args]
  (debug (sh-command project args))
  (apply eval/sh (sh-command project args)))
