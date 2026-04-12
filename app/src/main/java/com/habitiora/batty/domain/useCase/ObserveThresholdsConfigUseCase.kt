package com.habitiora.batty.domain.useCase

import com.habitiora.batty.domain.repository.ThresholdsRepository
import javax.inject.Inject

class ObserveThresholdsConfigUseCase @Inject constructor(
    private val repository: ThresholdsRepository
) {
    operator fun invoke() = repository.observe()
}