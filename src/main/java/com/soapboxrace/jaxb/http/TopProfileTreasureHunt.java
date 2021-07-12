package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Хранит список профилей с количеством дней, которые он подряд собирал кристалики
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "THTopProfile", propOrder = {
	"thProfileData"
})
public class TopProfileTreasureHunt {

	@XmlElement(name = "THProfile", nillable = true)
	protected List<ProfileDataTreasureHunt> thProfileData;
	
	public TopProfileTreasureHunt() {
		thProfileData = new ArrayList<ProfileDataTreasureHunt>();
	}
	/**
	 * Добавить профиль в список
	 * @param thProfile - объект профиля
	 */
	public void add(ProfileDataTreasureHunt thProfile) {
		thProfileData.add(thProfile);
	}
	/**
	 * Профиль с именем, иконкой и количеством дней, которые он подряд собирал кристалики
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "thprofile", propOrder = {
		"iconindex",
		"name",
		"treasure",
	})
	public static class ProfileDataTreasureHunt {
		@XmlElement(name = "Name")
		protected String name;
		@XmlElement(name = "IconIndex")
		protected int iconindex;
		@XmlElement(name = "Treasure")
		protected int treasure;
		public ProfileDataTreasureHunt(String name, int iconID, int treasure) {
			this.name = name;
			this.iconindex = iconID;
			this.treasure = treasure;
		}
		/**
		 * Получить имя профиля
		 */
		public String getName() {
			return name;
		}
		/**
		 * Получить ID иконки
		 */
		public int getIconIndex() {
			return iconindex;
		}
		/**
		 * Сколько дней подряд пользователь собирал кристалики
		 */
		public int getTreasure() {
			return treasure;
		}
	}
}
