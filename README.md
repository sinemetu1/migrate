# migrate

A Clojure library designed to perform simple migrations of clojure applications.

## Usage

Use this for user-level plugins:

Put `[migrate "0.1.0"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
migrate 0.1.0`.

Use this for project-level plugins:

Put `[migrate "0.1.0"]` into the `:plugins` vector of your project.clj.

### Migration

    $ cd my-app/src
    $ mkdir migrate
    
    ;; in /my-app/src/migrate/0001_setup.clj
    (ns migrate.0001-setup)
    
    ;; the do-migrate fn receives the direction of the migration
    ;; true being up and false being down
    (defn do-migrate
      [direction]
      (println "0001 setup" direction))

    ;; in /my-app/src/migrate/0002_next.clj
    (ns migrate.0002-next)
    
    ;; we can use the direction to make decisions
    (defn do-migrate
      [direction]
      (println "0002 next" (if direction
                             "moving up"
                             "moving down")))

### Migrate

    $ lein run migrate
    $ lein run migrate 0
    $ lein run migrate latest
    $ lein run migrate 2

## License

Copyright © 2013 Sam Garrett

Distributed under the Eclipse Public License, the same as Clojure.
