package com.nghivo.thcontroller.model

object BaseRequest {
    @JvmStatic
    fun get402Request(on: Boolean) = "{\"id_device\":\"node1\",\"modetemp\":${if (on) 1 else 0},\"req_device\":\"402\"}"

    @JvmStatic
    fun get403Request(baseResponse: BaseResponse?): String {
        val fan1 = baseResponse?.fan1 ?: 0
        val fan2 = baseResponse?.fan2 ?: 0
        val gianhiet = baseResponse?.gianhiet ?: 0
        val phun = baseResponse?.phun ?: 0
        return "{\"id_device\":\"node1\",\"fan1\":$fan1,\"fan2\":$fan2,\"phun\":$phun,\"gianhiet\":$gianhiet,\"req_device\":\"403\"}"
    }

    @JvmStatic
    fun get404Request(baseResponse: BaseResponse?): String {
        val maxTemp = baseResponse?.temptren ?: 0
        val minTemp = baseResponse?.tempduoi ?: 0
        val maxHumi = baseResponse?.humitren ?: 0
        val minHumi = baseResponse?.humiduoi ?: 0
        return "{\"id_device\":\"node1\",\"temptren\":$maxTemp,\"tempduoi\":$minTemp,\"humitren\":$maxHumi,\"humiduoi\":$minHumi,\"req_device\":\"404\"}";
    }

    @JvmStatic
    fun get411Request(baseResponse: BaseResponse?) = baseResponse
            ?.get411RequestString()
                    ?: "{\"id_device\":\"node1\",\"pass\":\"node1\",\"req_device\":401}"

    @JvmStatic
    fun getRegisterRequest(username: String, password: String) =
            "{\"user\":\"$username\",\"password\":\"$password\",\"req_device\":\"312\"}"

    @JvmStatic
    fun getLoginRequest(username: String, password: String) =
            "{\"user\":\"$username\",\"password\":\"$password\",\"req_device\":\"311\"}"

}