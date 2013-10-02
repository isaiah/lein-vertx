(ns lein-vertx.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [leiningen.core.classpath :as classpath]
            [leiningen.core.eval :as eval]
            [leiningen.core.user :as user]))

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
  {:dependencies '[[io.vertx/vertx-platform "2.0.1-final"]]
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
  (str/join java.io.File/pathSeparatorChar
            (concat
             (classpath/get-classpath project)
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

(defn ^:internal write-main
  "Writes out a verticle main to the compile-path that will invoke [:vertx :main-fn] from project."
  [project]
  (let [verticle-name "verticle_main.clj"
        compile-dir (doto (io/file (:compile-path project))
                      .mkdirs)]
    (spit (io/file compile-dir verticle-name)
          (str (synthesize-main (-> project :vertx :main-fn))
               "\n"))
    verticle-name))

(defn invoke-vertx
  "Invokes vertx in the given project."
  [project & args]
  (apply eval/sh (sh-command project args)))

(defn verticlize [x]
  (str/join (map str/capitalize (str/split x #"\."))))
