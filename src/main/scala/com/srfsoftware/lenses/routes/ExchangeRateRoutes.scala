package com.srfsoftware.lenses.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.srfsoftware.lenses.actor.RateConversionActor.{Action, ActionPerformed, CreateConversion, ErrorAction}
import com.srfsoftware.lenses.domain.Conversion
import com.srfsoftware.lenses.json.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait ExchangeRateRoutes extends JsonSupport {

  implicit def system: ActorSystem
  def rateConversionActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)
  lazy val log = Logging(system, classOf[ExchangeRateRoutes])

  def eh: ExceptionHandler =
    ExceptionHandler {
      case _ =>
        extractRequest { req =>
          log.error(s"$req")
          complete(HttpResponse(InternalServerError, entity = s"error on the request - ${req.entity}"))
        }
    }

  lazy val routes: Route = handleExceptions(eh) {
    path("api" / "convert") {
      concat(
        post {
          entity(as[Conversion]) { conv =>
            val convCreated: Future[Action] =
              (rateConversionActor ? CreateConversion(conv)).mapTo[Action]
            onComplete(convCreated) {
              case Success(value) => value match {
                case e:ErrorAction => {
                  log.error(e.error)
                  complete(StatusCodes.BadRequest, e)
                }
                case e:ActionPerformed => complete(StatusCodes.Created, e)
              }
              case Failure(exception) => {
                log.error(s"${exception.getMessage}")
                complete(StatusCodes.BadRequest, exception.asInstanceOf[ErrorAction])
              }
            }
          }
        }
      )
    }~ path(Remaining) { _ =>
        complete(StatusCodes.NotFound)
    }
  }
}