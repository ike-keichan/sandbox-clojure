(ns utils.mock-request.core)

(defn mock-request
  "Creates a mock HTTP request."
  [method path & {:as opts}]
  {:request-method method
   :uri path
   :query-string (opts :query-string)
   :headers (opts :headers)
   :body (opts :body)})
