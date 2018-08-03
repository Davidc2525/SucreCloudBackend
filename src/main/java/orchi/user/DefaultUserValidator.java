package orchi.user;

import orchi.SucreCloud.Start;
import orchi.user.Exceptions.EmailValidationException;
import orchi.user.Exceptions.FirstNameValidationException;
import orchi.user.Exceptions.LastNameValidationException;
import orchi.user.Exceptions.PasswordValidationException;
import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserNotExistException;
import orchi.user.Exceptions.UsernameValidationException;
import orchi.user.Exceptions.ValidationException;

public class DefaultUserValidator implements UserValidator {

	@Override
	public void validateEmail(User user) throws ValidationException {
		String email = user.getEmail();
		if (email.equalsIgnoreCase("")) {
			throw new EmailValidationException("El email no puede estar vacio.");
		}

		if (email.indexOf("@") == -1) {
			throw new EmailValidationException("El email ingresado no es valido.");
		}

		try {
			Start.getUserManager().getUserProvider().getUserByEmail(email);
			throw new EmailValidationException("Ya existe un usuario registrado con este email.");
		} catch (UserNotExistException e) {
			// inoramos
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

		try {
			Start.getUserManager().getUserProvider().getUserByUsername(username);
			throw new UsernameValidationException("Ya existe un usuario registrado con este username.");
		} catch (UserNotExistException e) {
			// inoramos
		} catch (UserException e) {
			throw new UsernameValidationException(e.getMessage());
		}
	}

	@Override
	public void validatePassword(User user) throws ValidationException {
		if (user.getPassword().equalsIgnoreCase("")) {
			throw new PasswordValidationException("La clave no puede estar vacia.");
		}
	}

	@Override
	public void validateFirstName(User user) throws ValidationException {
		DataUser u = (DataUser) user;
		if (u.getFirstName().equalsIgnoreCase("")) {
			throw new FirstNameValidationException("El nombre no puede estar vacio.");
		}

	}

	@Override
	public void validateLastName(User user) throws ValidationException {
		DataUser u = (DataUser) user;
		if (u.getLastName().equalsIgnoreCase("")) {
			throw new LastNameValidationException("El ultimo nombre no puede estar vacio.");
		}
	}

}
