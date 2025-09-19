package com.happyrow.core.infrastructure.event.common.error

import kotlin.reflect.KClass

data class EntityParsingException(
  val kclass: KClass<*>,
  override val cause: Throwable? = null,
) : Exception("Failed to build entity of $kclass")
