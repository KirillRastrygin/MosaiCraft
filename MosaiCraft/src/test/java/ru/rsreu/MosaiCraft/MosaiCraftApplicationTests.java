package ru.rsreu.MosaiCraft;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;
import org.springframework.boot.test.context.SpringBootTest;
import ru.rsreu.MosaiCraft.entities.*;
import ru.rsreu.MosaiCraft.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
class MosaiCraftApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void addNewUser() {
		Role role = new Role(1l,"test");
		UserService userService = new UserService();
		User user = User.builder()
				.username("admin")
				.password("admin")
				.email("admin@gmail.com")
				.build();
		user.setRoles((Set<Role>) role);
		userService.saveUser(user);
	}

	@Test
	void updateUser() {
		UserService userService = new UserService();

		userService.updateUser(userService.findUser(1));
	}

	@Test
	void deleteAllUsers() {
		UserService userService = new UserService();
		List<User> users = userService.findAllUsers();
		for (User user : users) {
			userService.deleteUser(user);
		}
	}

	@Test
	void addNew() {
		UserService userService = new UserService();
		User user = new User("TestTest", "TestTest", "TestTest");


		Template template = new Template("test", true);

		user.addTemplate(template);
		userService.saveUser(user);
		Role role = new Role(1l,"test");user.addRole(role);
		Image image = Image.builder()
				.imagePath("Test")
				.red(2d)
				.green(3d)
				.blue(5d)
				.build();
		template.addImage(image);

		Album album = Album.builder()
				.name("Test")
				.build();
		user.addAlbum(album);

		Mosaic mosaic = Mosaic.builder()
				.name("Test")
				.mosaicPath("Test")
				.build();

		user.addMosaic(mosaic);

		userService.updateUser(user);

	}

}
