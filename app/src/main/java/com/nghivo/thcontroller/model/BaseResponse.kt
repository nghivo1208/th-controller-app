package com.nghivo.thcontroller.model

import com.google.gson.annotations.SerializedName

// {"id_device":"node1","modetemp":0,"temptren":1,"tempduoi":0,"humitren":2,"humiduoi":0,"fan1":0,"fan2":1,"phun":0,"gianhiet":0}
open class BaseResponse(
        @SerializedName("modetemp") open var modetemp: Int,
        @SerializedName("temp") open var temp: Float,
        @SerializedName("humi") open var humi: Float,
        @SerializedName("temptren") open var temptren: Float,
        @SerializedName("tempduoi") open var tempduoi: Float,
        @SerializedName("humitren") open var humitren: Float,
        @SerializedName("humiduoi") open var humiduoi: Float,
        @SerializedName("fan1") open var fan1: Int,
        @SerializedName("fan2") open var fan2: Int,
        @SerializedName("phun") open var phun: Int,
        @SerializedName("gianhiet") open var gianhiet: Int,
        @SerializedName("id_device") open val idDevice: String = "node1"
) {

    fun get411RequestString() =
            "{\"id_device\":\"node1\",\"pass\":\"node1\",\"req_device\":401}"

}
