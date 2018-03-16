package webreduce.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public enum TableType implements Serializable {
	@SerializedName("LAYOUT")
	LAYOUT,

    @SerializedName("RELATION")
    RELATION,

    @SerializedName("MATRIX")
	MATRIX,

    @SerializedName("ENTITY")
    ENTITY,

    @SerializedName("OTHER")
    OTHER
}
