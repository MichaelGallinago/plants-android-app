package net.micg.plantcare.domain.implementations

import net.micg.plantcare.data.article.ArticlesRepository
import net.micg.plantcare.domain.usecase.GetAllArticlesUseCase

class GetAllArticlesUseCaseImpl(private val repository: ArticlesRepository) :
    GetAllArticlesUseCase {
    override suspend operator fun invoke() = repository.getAll()
}
