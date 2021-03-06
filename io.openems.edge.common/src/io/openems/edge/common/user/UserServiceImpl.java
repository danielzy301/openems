package io.openems.edge.common.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.session.Role;

/**
 * This component handles User authentication.
 */
@Designate(ocd = Config.class, factory = false)
@Component(name = "Core.User", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class UserServiceImpl implements UserService {

	private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	/**
	 * All configured users. Ordered as they are added.
	 */
	private final List<User> users = new ArrayList<>();

	@Activate
	void activate(Config config) {
		this.users.add(//
				new User("admin", Role.ADMIN, config.adminPassword(), config.adminSalt()));
		this.users.add(//
				new User("installer", Role.INSTALLER, config.installerPassword(), config.installerSalt()));
		this.users.add(//
				new User("owner", Role.OWNER, config.ownerPassword(), config.ownerSalt()));
		this.users.add(//
				new User("guest", Role.GUEST, config.guestPassword(), config.guestSalt()));
	}

	@Deactivate
	void deactivate() {
	}

	@Override
	public final Optional<User> authenticate(String username, String password) {
		// Search for user with given username
		for (User user : this.users) {
			if (username.equals(user.getName())) {
				if (user.validatePassword(password)) {
					log.info("Authentication successful for user[" + username + "].");
					return Optional.of(user);
				} else {
					log.info("Authentication failed for user[" + username + "]: wrong password");
					return Optional.empty();
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public final Optional<User> authenticate(String password) {
		// Search for any user with the given password
		for (User user : this.users) {
			if (user.validatePassword(password)) {
				log.info("Authentication successful with password only for user [" + user.getName() + "].");
				return Optional.ofNullable(user);
			}
		}
		log.info("Authentication failed with password only.");
		return Optional.empty();
	}

	// private static byte[] getRandomSalt(int length) {
	// SecureRandom sr = SecureRandomSingleton.getInstance();
	// byte[] salt = new byte[length];
	// sr.nextBytes(salt);
	// return salt;
	// }

	// TODO implement change password
	// public void changePassword(String oldPassword, String newPassword) throws
	// OpenemsException {
	// if (checkPassword(oldPassword)) {
	// byte[] salt = getRandomSalt(SALT_LENGTH);
	// byte[] password = hashPassword(newPassword, salt);
	// this.password = password;
	// this.salt = salt;
	// // Config.getInstance().writeConfigFile();
	// } else {
	// throw new OpenemsException("Access denied. Old password was wrong.");
	// }
	// }

}
