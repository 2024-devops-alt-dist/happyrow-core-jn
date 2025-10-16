package com.happyrow.core.infrastructure.event.delete.driving

import arrow.core.Either
import arrow.core.flatMap
import com.happyrow.core.domain.event.common.error.DeleteEventRepositoryException
import com.happyrow.core.domain.event.common.error.EventNotFoundException
import com.happyrow.core.domain.event.delete.DeleteEventUseCase
import com.happyrow.core.domain.event.delete.error.DeleteEventException
import com.happyrow.core.infrastructure.event.common.error.BadRequestException
import com.happyrow.core.infrastructure.technical.ktor.ClientErrorMessage
import com.happyrow.core.infrastructure.technical.ktor.ClientErrorMessage.Companion.technicalErrorMessage
import com.happyrow.core.infrastructure.technical.ktor.logAndRespond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import java.util.UUID

private const val EVENT_NOT_FOUND_ERROR_TYPE = "EVENT_NOT_FOUND"

fun Route.deleteEventEndpoint(deleteEventUseCase: DeleteEventUseCase) {
  delete("/{id}") {
    val eventId = Either.catch {
      UUID.fromString(call.parameters["id"])
    }.mapLeft { BadRequestException.InvalidParameterException("id", call.parameters["id"] ?: "null") }

    eventId.flatMap { id -> deleteEventUseCase.delete(id) }
      .fold(
        { it.handleFailure(call) },
        { call.respond(HttpStatusCode.NoContent) },
      )
  }
}

private suspend fun Exception.handleFailure(call: ApplicationCall) = when (this) {
  is BadRequestException -> call.logAndRespond(
    status = HttpStatusCode.BadRequest,
    responseMessage = ClientErrorMessage.of(type = type, detail = message),
    failure = this,
  )

  is DeleteEventException -> this.handleFailure(call)

  else -> call.logAndRespond(
    status = HttpStatusCode.InternalServerError,
    responseMessage = technicalErrorMessage(),
    failure = this,
  )
}

private suspend fun DeleteEventException.handleFailure(call: ApplicationCall) = when (cause) {
  is DeleteEventRepositoryException -> (cause as DeleteEventRepositoryException).handleFailure(call)

  else -> call.logAndRespond(
    status = HttpStatusCode.InternalServerError,
    responseMessage = technicalErrorMessage(),
    failure = this,
  )
}

private suspend fun DeleteEventRepositoryException.handleFailure(call: ApplicationCall) = when (cause) {
  is EventNotFoundException -> call.logAndRespond(
    status = HttpStatusCode.NotFound,
    responseMessage = ClientErrorMessage.of(
      type = EVENT_NOT_FOUND_ERROR_TYPE,
      detail = "Event with id $identifier not found",
    ),
    failure = this,
  )

  else -> call.logAndRespond(
    status = HttpStatusCode.InternalServerError,
    responseMessage = technicalErrorMessage(),
    failure = this,
  )
}
