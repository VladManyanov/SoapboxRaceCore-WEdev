package com.soapboxrace.jaxb.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Хранит список профилей с количество очков
 * @author Vadimka
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DSTopProfile", propOrder = {
	"dsProfileData"
})
public class TopProfileScore {

	@XmlElement(name = "DSProfile", nillable = true)
	protected List<ProfileDataScore> dsProfileData;
	
	public TopProfileScore() {
		dsProfileData = new ArrayList<ProfileDataScore>();
	}
	/**
	 * Добавить профиль в список
	 * @param profile - объект профиля
	 */
	public void add(ProfileDataScore dsProfile) {
		dsProfileData.add(dsProfile);
	}
	/**
	 * Профиль с именем, иконкой и количеством очков
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "dsprofile", propOrder = {
		"iconindex",
		"name",
		"score",
	})
	public static class ProfileDataScore {
		@XmlElement(name = "Name")
		protected String name;
		@XmlElement(name = "IconIndex")
		protected int iconindex;
		@XmlElement(name = "Score")
		protected int score;
		public ProfileDataScore(String name, int iconID, int score) {
			this.name = name;
			this.iconindex = iconID;
			this.score = score;
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
		 * Получить очки профиля
		 */
		public int getScore() {
			return score;
		}
	}
}
