(ns web-api.servidor
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [web-api.database :as database]
            [clojure.edn :as edn]))

(defn funcao-hello [request]  
  {:status 200 :body (str "Oi Mudão de meu Deus!!" (get-in  request [:query-params :name] "Tests"))}
  )


(defn create-task-map [uuid nome status]
  {:uuid uuid :nome nome :status status})

(defn create-task [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        tarefa (create-task-map uuid nome status)
        store (:store request)]
    (swap! store assoc uuid tarefa)
    {:status 201 :body {:mensagem "Tarefa Crinhada com Sucesso" :tarefa tarefa}}))

(defn list-task [request]
  {:status 200 :body {:mensagem "Cconsulta ok" :store @(:store request)}})



(defn assoc-store [context]
  (update context :request assoc :store database/store)
  )

(def db-interceptor
  {
   :name :db-interceptor
   :enter assoc-store
   
  })

(def routes (route/expand-routes 
             #{["/hello" :get funcao-hello :route-name :hello-world]
               ["/task" :post [db-interceptor create-task] :route-name :create-task]
               ["/task" :get [db-interceptor list-task] :route-name :list-task]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?    false}
                  )

(defonce server (atom nil))



(defn start-server []
  (reset! server (http/start (http/create-server service-map)))
  )

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))


(defn stop []
  (http/stop @server))

(defn restart-server []
  (stop)
  (start-server))

;; (stop)
;; (start-server)
;; (restart-server)


(test-request :get "/hello?name= Waldiney")
;; (test-request :post "/task?name= Terminar Api Clojure&status=onGoing")
;; (test-request :post "/task?name= Rule Cassandra&status=Pendente")
;; (read-string (:body (test-request :get "/task")))



;; (println @database/store)
