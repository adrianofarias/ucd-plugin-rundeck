//Documentacao GitHub: https://github.com/jgritman/httpbuilder/wiki/RESTClient


import groovyx.net.http.RESTClient
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

public class HttpHelper {

    def rest
    def rest_method
    def rest_payload
    String rest_response

    public HttpHelper(String url) {
        rest = new RESTClient(url)
        rest.ignoreSSLIssues()
        rest_method = GET
    }

    public HttpHelper(String url, Object method, Object payload) {
        rest = new RESTClient(url)
        rest.ignoreSSLIssues()
        rest_method = method
        rest_payload = payload
    }

    void setCredenciais(String username, String password)   {
        rest.setHeaders([Authorization: "Basic "+Base64.getEncoder().encodeToString("${username}:${password}".getBytes())])
    }

    String getResponse()   {

        rest.request(rest_method,TEXT) { req ->
            switch(rest_method) {
                case POST:
                    if (rest_payload) {
                        body = rest_payload            
                    }
                break
            }
            response.success = { resp, reader ->
                assert resp.status < 400
                rest_response = reader.text
            }
            response.failure = { resp ->
                println 'ERRO: '+resp.status+'. Requisição falhou.'
                assert resp.status >= 400
                System.exit(1)
            }
            response.'401' = { resp ->
                println 'ERRO: Acesso negado. Usuário ou senha inválidos.'
                assert resp.status == 401
                System.exit(1)
            }
            response.'403' = { resp ->
                println 'ERRO: Não autorizado. Pode ser que seu token tenha expirado.'
                assert resp.status == 403
                System.exit(1)
            }
        }

        return rest_response
    }
}