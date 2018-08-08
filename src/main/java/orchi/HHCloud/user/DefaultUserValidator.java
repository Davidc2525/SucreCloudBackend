package orchi.HHCloud.user;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.RegexValidator;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.Exceptions.EmailValidationException;
import orchi.HHCloud.user.Exceptions.FirstNameValidationException;
import orchi.HHCloud.user.Exceptions.GenderValidationException;
import orchi.HHCloud.user.Exceptions.LastNameValidationException;
import orchi.HHCloud.user.Exceptions.PasswordValidationException;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
import orchi.HHCloud.user.Exceptions.UsernameValidationException;
import orchi.HHCloud.user.Exceptions.ValidationException;

public class DefaultUserValidator implements UserValidator {
	private EmailValidator Emailvalidator;
	private RegexValidator Stringvalidator;
	private RegexValidator passValidator;
	private RegexValidator FirstAndLastNameValidator;

	public DefaultUserValidator() {
		//validador de correos
		Emailvalidator = EmailValidator.getInstance();
		boolean caseSensitive = false;
		//String regex1 = "^([A-Z]*)(?:\\-)([A-Z]*)*$";
		//String regex2 = "^([A-Z]*)$";
		String regex3 = "^([-_A-Za-z0-9]){5,25}$";
		String[] regexs = new String[] {regex3};
		
		//Validador de username
		Stringvalidator = new RegexValidator(regexs, caseSensitive);		
		
		
		String regexPass = "^([A-Za-z0-9_.,&%€@#~]){8,}$";
		String[] regexsPass = new String[] {regexPass};
		//validador para claves
		passValidator = new RegexValidator(regexsPass, caseSensitive);
		
		String regexFirstAndLastName = "^([-_A-Za-z0-9]){2,15}$";		
		FirstAndLastNameValidator = new RegexValidator(regexFirstAndLastName);

	}

	@Override
	public void validateEmail(User user) throws ValidationException {
		String email = user.getEmail();
		if (email.equalsIgnoreCase("")) {
			throw new EmailValidationException("El email no puede estar vacio.");
		}

		if (!Emailvalidator.isValid(email)) {
			throw new EmailValidationException("El email ingresado no es valido. !(isValid)");
		}

		try {
			Start.getUserManager().getUserProvider().getUserByEmail(email);
			throw new EmailValidationException("Ya existe un usuario registrado con este email.");
		} catch (UserNotExistException e) {
			// ignoramos
		} catch (UserException e) {
			throw new EmailValidationException(e.getMessage());
		}
	}

	@Override
	public void validateUsername(User user) throws ValidationException {
		String username = user.getUsername();
		if (username.equalsIgnoreCase("")) {
			throw new UsernameValidationException("El username no puede estar vacio.");
		}
		
		if(!Stringvalidator.isValid(username)){
			throw new UsernameValidationException("El username no es valido. !(isValid)");
		}
		

		try {
			Start.getUserManager().getUserProvider().getUserByUsername(username);
			throw new UsernameValidationException("Ya existe un usuario registrado con este username.");
		} catch (UserNotExistException e) {
			// ignoramos
		} catch (UserException e) {
			throw new UsernameValidationException(e.getMessage());
		}
	}

	@Override
	public void validatePassword(User user) throws ValidationException {
		if (user.getPassword().equalsIgnoreCase("")) {
			throw new PasswordValidationException("La clave no puede estar vacia.");
		}
		
		if(!passValidator.isValid(user.getPassword())){
			throw new PasswordValidationException("La clave es invalida.");
		}
	}

	@Override
	public void validateFirstName(User user) throws ValidationException {
		DataUser u = (DataUser) user;
		if (u.getFirstName().equalsIgnoreCase("")) {
			throw new FirstNameValidationException("El nombre no puede estar vacio.");
		}
		
		if(!FirstAndLastNameValidator.isValid(u.getFirstName())){
			throw new FirstNameValidationException("El nombre no es valido.");
		}

	}

	@Override
	public void validateLastName(User user) throws ValidationException {
		DataUser u = (DataUser) user;
		if (u.getLastName().equalsIgnoreCase("")) {
			throw new LastNameValidationException("El apellido no puede estar vacio.");
		}
		
		if(!FirstAndLastNameValidator.isValid(u.getLastName())){
			throw new LastNameValidationException("El apellido no es valido.");
		}
	}
	
	public void validateGender(User user) throws ValidationException {
		DataUser u = (DataUser) user;
		String gender = u.getGender();
		System.out.println(u);
		if (!gender.equals("")) {
			switch (gender) {
			case "f"://femenino
			case "m"://masculino
			case "n"://no desea espesificar
				break;

			default:
				throw new GenderValidationException("El genero solo puede ser (f) ó (m), si no desea espesificar, (n)");
			}

		}else{
			throw new GenderValidationException("El genero solo puede ser (f) ó (m), si no desea espesificar, (n)");
		}
	}

}
