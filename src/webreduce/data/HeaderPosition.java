package webreduce.data;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public enum HeaderPosition implements Serializable {
	@SerializedName("FIRST_ROW")
	FIRST_ROW,

	@SerializedName("FIRST_COLUMN")
	FIRST_COLUMN,

	@SerializedName("NONE")
	NONE,

	@SerializedName("MIXED")
	MIXED
}
