{:jig/components
 {
  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../hecuba/project.clj"
   :jig/dependencies []
   :name "Bruce!!"
;;   :jig.web/context "/services"
   }

  #_:hecuba/cljs-builder
  #_{:jig/component jig.cljs-builder/Builder
   :jig/project "../hecuba/project.clj"
   :output-dir "../hecuba/target/js"
   :output-to "../hecuba/target/js/main.js"
   :source-map "../hecuba/target/js/main.js.map"
   :optimizations :none
   }

  #_:hecuba/cljs-server
  #_{:jig/component jig.cljs/FileServer
   :jig/dependencies [:hecuba/cljs-builder]
   :jig.web/context "/js"
   }

  :hecuba/routing
  {:jig/component jig.bidi/Router
   :jig/project "../hecuba/project.clj"
   :jig/dependencies [:hecuba/website #_:hecuba/cljs-server]
   ;; Optionally, route systems can be mounted on a sub-context
   :jig.web/context "/services"
   }

  :hecuba/webserver
  {:jig/component jig.http-kit/Server
   :jig/project "../hecuba/project.clj"
   :jig/dependencies [:hecuba/routing]
   :port 8000}

}}
