package com.example.goalplanningapp.entity;

// データ型を定義するクラス　enumeration(列挙)
public enum Gender {
	//男性、女性、その他
	MALE(0),
	FEMALE(1),
	OTHER(2);
	
	// フィールドの定義
	private final int code;
	
	// コンストラクタの定義
	Gender(int code){this.code =code;}
	
	// DBに保存している整数値を取得
	public int getCode() {
		return code;
	}
	// DBの値からEnumに変換するメソッド
	public static Gender fromCode(int code) {
		for (Gender g :values()) {
			if (g.code == code) {
				return g;
			}
		}
		return OTHER;  //デフォルト
	}

}