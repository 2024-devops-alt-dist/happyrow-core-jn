package com.happyrow.core.infrastructure.resource.create.driving.dto

import com.happyrow.core.domain.resource.common.model.ResourceCategory
import com.happyrow.core.domain.resource.create.model.CreateResourceRequest
import java.util.UUID

class CreateResourceRequestDto(
  val name: String,
  val category: String,
  val quantity: Int,
  val suggestedQuantity: Int? = null,
) {
  fun toDomain(eventId: UUID, userId: UUID): CreateResourceRequest = CreateResourceRequest(
    name = this.name,
    category = ResourceCategory.valueOf(this.category),
    initialQuantity = this.quantity,
    eventId = eventId,
    userId = userId,
    suggestedQuantity = this.suggestedQuantity ?: this.quantity,
  )
}
