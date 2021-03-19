package com.soapboxrace.core.bo.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;

import com.soapboxrace.core.dao.PersonaDAO;
import com.soapboxrace.core.jpa.PerformancePartEntity;
import com.soapboxrace.core.jpa.SkillModPartEntity;

@Stateless
public class StringListConverter {
	
	@EJB
	private PersonaDAO personaDAO;

	public String listToStr(List<Long> items) {
		if (items.isEmpty()) {
			return null;
		}
		String list = StringUtils.join(items, ',');
		return list;
	}
	
	public String interceptorPersonaChatList(List<Long> personas) {
		if (personas.isEmpty()) {
			return "N/A";
		}
		StringBuilder sbArray = new StringBuilder();
		for (Long personaId : personas)
		{
			sbArray.append(personaDAO.findById(personaId).getName());
			sbArray.append(" ");
		}
		return sbArray.toString();
	}
	
	public String skillModsStrArray(Set<SkillModPartEntity> skillModsArray) {
		int itemCount = skillModsArray.size();
		if (itemCount == 0) {
			return null;
		}
		Integer[] skillModsIntArray = new Integer[itemCount];
		Iterator<SkillModPartEntity> skillModsIterArray = skillModsArray.iterator();
		int i = 0;
		while (skillModsIterArray.hasNext()) {
			skillModsIntArray[i] = skillModsIterArray.next().getSkillModPartAttribHash();
			i++;
		}
		return hashItemsList(skillModsIntArray);
	}
	
	public String perfPartsStrArray(Set<PerformancePartEntity> perfArray) {
		int itemCount = perfArray.size();
		if (itemCount == 0) {
			return null;
		}
		Integer[] perfPartsIntArray = new Integer[itemCount];
		Iterator<PerformancePartEntity> perfPartsIterArray = perfArray.iterator();
		int i = 0;
		while (perfPartsIterArray.hasNext()) {
			perfPartsIntArray[i] = perfPartsIterArray.next().getPerformancePartAttribHash();
			i++;
		}
		return hashItemsList(perfPartsIntArray);
	}
	
	public String hashItemsList(Integer[] array) {
		String list = StringUtils.join(array, ',');
		return list;
	}
	
	public Long[] StrToLongList(String[] strList) {
		Long[] longList = new Long[strList.length];
		for (int i = 0; i < strList.length; i++)
			longList[i] = Long.parseLong(strList[i]);
		return longList;
	}
	
	public Integer[] StrToIntList(String[] strList) {
		Integer[] intList = new Integer[strList.length];
		for (int i = 0; i < strList.length; i++)
			intList[i] = Integer.parseInt(strList[i]);
		return intList;
	}

}
