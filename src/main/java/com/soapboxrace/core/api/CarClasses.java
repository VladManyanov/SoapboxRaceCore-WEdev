package com.soapboxrace.core.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.soapboxrace.core.bo.util.CarClassType;
import com.soapboxrace.jaxb.http.ArrayOfCarClass;
import com.soapboxrace.jaxb.http.CarClass;

@Path("/carclasses")
public class CarClasses {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ArrayOfCarClass carClasses() {
		ArrayOfCarClass arrayOfCarClass = new ArrayOfCarClass();
		CarClass carClassS = new CarClass();
		carClassS.setCarClassHash(CarClassType.S_CLASS.getId());
		carClassS.setMaxRating((short) 999);
		carClassS.setMinRating((short) 750);
		arrayOfCarClass.getCarClass().add(carClassS);
		CarClass carClassA = new CarClass();
		carClassA.setCarClassHash(CarClassType.A_CLASS.getId());
		carClassA.setMaxRating((short) 749);
		carClassA.setMinRating((short) 600);
		arrayOfCarClass.getCarClass().add(carClassA);
		CarClass carClassB = new CarClass();
		carClassB.setCarClassHash(CarClassType.B_CLASS.getId());
		carClassB.setMaxRating((short) 599);
		carClassB.setMinRating((short) 500);
		arrayOfCarClass.getCarClass().add(carClassB);
		CarClass carClassC = new CarClass();
		carClassC.setCarClassHash(CarClassType.C_CLASS.getId());
		carClassC.setMaxRating((short) 499);
		carClassC.setMinRating((short) 400);
		arrayOfCarClass.getCarClass().add(carClassC);
		CarClass carClassD = new CarClass();
		carClassD.setCarClassHash(CarClassType.D_CLASS.getId());
		carClassD.setMaxRating((short) 399);
		carClassD.setMinRating((short) 250);
		arrayOfCarClass.getCarClass().add(carClassD);
		CarClass carClassE = new CarClass();
		carClassE.setCarClassHash(CarClassType.E_CLASS.getId());
		carClassE.setMaxRating((short) 249);
		carClassE.setMinRating((short) 1);
		arrayOfCarClass.getCarClass().add(carClassE);
		return arrayOfCarClass;
	}
}
