(ns migrate.core)

(def ^{:private true} dir "src/migrate")
(def ^{:private true} migrate-files [])
(def ^{:private true} level-file-name (format "%s/_level.txt" dir))

(declare do-migrate) ;; this is used by all the migration scripts

(defn- format-level
  [a-level]
  (format "%04d" (Integer. a-level)))

(defn- get-level-from-file
  [file]
  (second (re-find #"([0-9]+).*" file)))

(defn- get-migration-files
  [files curr dest up?]
  (let [dest-level (Integer. (format-level dest))]
    (for [file files
          :let [curr-level (Integer. (get-level-from-file file))]
          :when (if up?
                  ;; moving up with curr = 2, dest = 5
                  ;;   must be <= 5 and >= 2
                  (and
                    (<= curr-level dest-level)
                    (> curr-level (Integer. curr)))
                  ;; moving down with curr = 5, dest = 2
                  ;;   must be >= 2 and <= 5
                  (and
                    (> curr-level dest-level)
                    (<= curr-level (Integer. curr))))]
      file)))

(defn- get-all-files
  []
  (let [file-list (.listFiles
                    (java.io.File. dir))
        whitelist (re-pattern "([0-9]{4,}).*.clj$")]
    (apply list
      (for [file  (sort file-list)
            :let  [a-name (.getName file)]
            :when (re-find whitelist a-name)]
        a-name))))

(defn- get-ns-from-filename
  [file-name]
  (clojure.string/replace (clojure.string/replace file-name ".clj" "") "_" "-"))

(defn- get-current-app-level
  []
  (let [_    (spit level-file-name "" :append true)
        curr (slurp level-file-name)]
    (Integer.
      (if (= curr "")
        0
        curr))))

(defn- get-destination-level
  [args files]
  (if (and args
           (not= (first args) "latest"))
    (Integer. (first args))
    (count files)))

(defn- load-migration-file
  [a-ns]
  (let [to-load (read-string (format "'migrate.%s" a-ns))]
    (require (eval to-load))))

(defn- execute-migration-level
  [a-ns up?]
  (let [to-do (format "(migrate.%s/do-migrate %s)" a-ns (str up?))]
    (println "Executing:" to-do)
    (load-string to-do)))

(defn- end-migration
  [dest]
  (spit level-file-name dest)
  (println (format "Final level is: %s" dest)))

(defn- work-to-do?
  [curr dest]
  (not= curr dest))

(defn- is-up?
  [curr dest]
  (> dest curr))

(defn -main 
  "Migrate an application to a specific point."
  [& args]
  (let [all-files (get-all-files)
        dest      (get-destination-level (seq args) all-files)
        curr      (get-current-app-level)
        up?       (is-up? curr dest)
        templist  (if up?
                    all-files
                    (reverse all-files))
        files  (get-migration-files templist curr dest up?)]

    (if (work-to-do? curr dest)
      (do
        (println "Migrating from:" curr "to dest:" dest)
        (doseq [file files]
          (let [a-ns (get-ns-from-filename file)]
            (load-migration-file a-ns)
            (execute-migration-level a-ns up?)))
        (end-migration dest))
      (println (format "Nothing to do. Current level is: %s" curr)))))
