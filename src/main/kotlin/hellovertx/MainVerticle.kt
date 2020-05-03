package hellovertx

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val router = createRouter()

    vertx
      .createHttpServer()
//      .requestHandler { req ->
//        req.response()
//          .putHeader("content-type", "text/plain")
//          .end("Hello from Vert.x!")
//      }
      .requestHandler(router) // 为所有请求指定 Handler，这里先走一遭 Router
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("启动 HTTP 服务器，端口： 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }

  // Router

  private fun createRouter() = Router.router(vertx).apply {
    get("/").handler(handlerRoot)
    get("/islands").handler(handlerIslands)
    get("/countries").handler(handlerCountries)
  }

  // Handlers

  val handlerRoot = Handler<RoutingContext> { req ->
    req.response().end("Welcome!")
  }

  val handlerIslands = Handler<RoutingContext> { req ->
    req.response().endWithJson(MOCK_ISLANDS)
  }

  val handlerCountries = Handler<RoutingContext> { req ->
    req.response().endWithJson(MOCK_ISLANDS.map { it.country }.distinct().sortedBy { it.code })
  }

  // Mock data

  private val MOCK_ISLANDS by lazy {
    listOf(
      Island("Kotlin", Country("Russia", "RU")),
      Island("Stewart Island", Country("New Zealand", "NZ")),
      Island("Cockatoo Island", Country("Australia", "AU")),
      Island("Tasmania", Country("Australia", "AU"))
    )
  }

  // Utilities

  /**
   * Extension to the HTTP response to output JSON objects.
   */
  fun HttpServerResponse.endWithJson(obj: Any) {
    this.putHeader("Content-Type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(obj.toString()))  // Vert.x 当前版本的 Json.encode() 有点问题
  }

}
