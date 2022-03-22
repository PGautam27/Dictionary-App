package com.example.dictionaryapp.feature_dictionary.data.repository

import com.example.dictionaryapp.core.util.Resource
import com.example.dictionaryapp.feature_dictionary.data.local.WordInfoDoa
import com.example.dictionaryapp.feature_dictionary.data.remote.DictionaryApi
import com.example.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.example.dictionaryapp.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WordInfoRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordInfoDoa
): WordInfoRepository {
    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        emit(Resource.Loading())

        val worInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = worInfos))

        try {
            val remoteWordInfos = api.getWordInfo(word)
            dao.delteWordInfos(remoteWordInfos.map { it.word })
            dao.insertWordInfos(remoteWordInfos.map { it.toWordInfoEntity() })
        }catch (e: HttpException){
            emit(Resource.Error(message = "HTTP ERROR Found", data = worInfos) )
        }catch (e: IOException){
            emit(Resource.Error(message = "Couldn't reach Server, check your internet connection", data = worInfos) )
        }

        val newWordInfos = dao.getWordInfos(word = word).map { it.toWordInfo() }
        emit(Resource.Success(newWordInfos))
    }
}