package com.example.appmusica.retrofit.user
import retrofit2.Response

class ApiService {
    suspend fun loginService(log: RequestUser): Result<ResponseUser> {
        try{
            val response = InstanceRetrofit.retrofitService.login(log)
            if (response.isSuccessful){
                response.body()?.let{
                        body -> return Result.success(body)
                }?: return Result.failure(RuntimeException("Respuesta de usuario nulo en el logueo"))
            }else {
                //aquí ha habido una respuesta por la api, con algún 40x
                val errorMsg = response.errorBody()?.string() ?: "Error no reconocido al loguear"
                return Result.failure(RuntimeException(errorMsg))
            }
        }catch (e : Exception) {
            return Result.failure(e)
        }

    }


    suspend fun registerService (user: RequestUser ): Result<ResponseUser>{
        try{
            val response = InstanceRetrofit.retrofitService.register(user)
            if (response.isSuccessful){
                response.body()?.let{
                    //Si el body no es nulo, tengo datos con mi 20x.
                    //Si el body es nulo, ha sido un 20x, pero no tengo respuesta de la api.
                        body -> return Result.success(body)
                }?: return Result.failure(RuntimeException("Respuesta de usuario nulo en el registro"))
            }else{
                //aquí ha habido una respuesta por la api, con algún 40x
                /*
                Cuando se produce un error 40x, por la api, Retrofit considerará la respuesta como
                fallida y por tanto pueden pasar dos cosas:
                1.- Que la api me haya mandado un mensaje de error personalizado, pero la petición es correcta.
                2.- Que la api sólo me haya mandado un mensaje de error por alguna excepción. Petición no correcta.
                 */
                val errorMsg = response.errorBody()?.string() ?: "Error no reconocido al registrar"
                return Result.failure(RuntimeException(errorMsg))
            }

        }catch (e : Exception) {
            return Result.failure(e)
        }
    }


    suspend fun getAllEmployee (token: String): Result<List<ResponseUser>>{
        try{
            val t = "Bearer "+token
            val response = InstanceRetrofit.retrofitService.getAll(t)
            if (response.isSuccessful){
                response.body()?.let{
                        body -> return Result.success(body)
                }?: return Result.failure(RuntimeException("body nulo"))

            }else{
                //Me puede mandar un error personalizado, pero la petición es correcta.
                //ó que la API me haya mandado un mensaje de error por alguna excepción. Petición no correcta.
                val errorMsg = response.errorBody()?.string() ?: "Error no reconocido al listar"   //error personalizado o Excepción
                return Result.failure(RuntimeException(errorMsg))
            }

        }catch (e: Exception){
            return Result.failure(e)
        }
    }
}