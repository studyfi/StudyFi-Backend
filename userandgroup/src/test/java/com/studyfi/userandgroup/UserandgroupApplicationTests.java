package com.studyfi.userandgroup;

import com.studyfi.userandgroup.group.dto.GroupDTO;
import com.studyfi.userandgroup.group.service.GroupService;
import com.studyfi.userandgroup.service.CloudinaryService;
import com.studyfi.userandgroup.user.dto.PasswordResetDTO;
import com.studyfi.userandgroup.user.dto.UserDTO;
import com.studyfi.userandgroup.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserandgroupApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private GroupService groupService;

	@MockBean
	private UserService userService;

	@MockBean
	private CloudinaryService cloudinaryService;

	@BeforeEach
	void setup() throws Exception {
		when(cloudinaryService.uploadFile(any())).thenReturn("http://example.com/image.jpg");
	}

	@Test
	public void testCreateGroup() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "some image".getBytes());
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setName("Test Group");
		groupDTO.setDescription("Test Description");
		groupDTO.setImageUrl("http://example.com/image.jpg");
		when(groupService.createGroup(any(GroupDTO.class))).thenReturn(groupDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/groups/create")
						.file(file)
						.param("name", "Test Group")
						.param("description", "Test Description"))
				.andExpect(status().isOk()); // Expect 200 for successful creation
	}

	@Test
	public void testUpdateGroup() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "some image".getBytes());
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setName("Updated Group");
		groupDTO.setDescription("Updated Description");
		groupDTO.setImageUrl("http://example.com/image.jpg");
		when(groupService.updateGroup(eq(1), any(GroupDTO.class))).thenReturn(groupDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/groups/update/1")
						.file(file)
						.param("name", "Updated Group")
						.param("description", "Updated Description")
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT");
							return request;
						}))
				.andExpect(status().isOk()); // Expect 200
	}

	@Test
	public void testGetAllGroups() throws Exception {
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setName("Test Group");
		groupDTO.setDescription("Test Description");
		when(groupService.getAllGroups()).thenReturn(Collections.singletonList(groupDTO));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/groups/all"))
				.andExpect(status().isOk()); // Expect 200 for successful retrieval
	}

	@Test
	public void testGetGroupById() throws Exception {
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setName("Test Group");
		groupDTO.setDescription("Test Description");
		when(groupService.getGroupById(1)).thenReturn(groupDTO);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/groups/1"))
				.andExpect(status().isOk()); // Expect 200 for successful retrieval
	}

	@Test
	public void testGetUsersByGroup() throws Exception {
		when(userService.getUsersByGroupId(1)).thenReturn(Arrays.asList(1, 2));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/groups/1/users"))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetGroupsByUser() throws Exception {
		when(groupService.getGroupsByUser(1)).thenReturn(Collections.singletonList(new GroupDTO()));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/groups/user/1"))
				.andExpect(status().isOk());
	}

	@Test
	public void testRegisterUser() throws Exception {
		MockMultipartFile profileFile = new MockMultipartFile("profileFile", "test.jpg", "image/jpeg", "some image".getBytes());
		MockMultipartFile coverFile = new MockMultipartFile("coverFile", "test.jpg", "image/jpeg", "some image".getBytes());
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("test@test.com");
		when(userService.registerUser(any(UserDTO.class))).thenReturn(userDTO);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/register")
						.file(profileFile)
						.file(coverFile)
						.param("name", "Test User")
						.param("email", "test@test.com")
						.param("password", "password")
						.param("phoneContact", "123")
						.param("birthDate", "12/12/12")
						.param("country", "brazil")
						.param("aboutMe", "Hi")
						.param("currentAddress", "here"))
				.andExpect(status().isOk());
	}

	@Test
	public void testLogin() throws Exception {
		when(userService.login(eq("test@test.com"), eq("password"))).thenReturn(new UserDTO());
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/login")
						.param("email", "test@test.com")
						.param("password", "password"))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetAllUsers() throws Exception {
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(new UserDTO()));
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/getusers"))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetUserById() throws Exception {
		when(userService.getUserById(1)).thenReturn(new UserDTO());
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/1"))
				.andExpect(status().isOk());
	}

	@Test
	public void testResetPassword() throws Exception {
		PasswordResetDTO passwordResetDTO = new PasswordResetDTO();
		passwordResetDTO.setNewPassword("newPassword");
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/reset-password")
						.param("token", "validToken")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"password\":\"newPassword\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testForgotPassword() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"test@test.com\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void testUpdateProfile() throws Exception {
		MockMultipartFile profileFile = new MockMultipartFile("profileFile", "test.jpg", "image/jpeg", "some image".getBytes());
		MockMultipartFile coverFile = new MockMultipartFile("coverFile", "test.jpg", "image/jpeg", "some image".getBytes());
		UserDTO userDTO = new UserDTO();
		userDTO.setName("some");
		userDTO.setEmail("test@test.com");
		userDTO.setPhoneContact("123");
		userDTO.setBirthDate("12/12/12");
		userDTO.setCountry("brazil");
		userDTO.setAboutMe("Hi");
		userDTO.setCurrentAddress("here");
		when(userService.updateUserProfile(eq(1), any(UserDTO.class))).thenReturn(userDTO);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/profile/1")
						.file(profileFile)
						.file(coverFile)
						.param("name", "some")
						.param("email", "test@test.com")
						.param("password", "password")
						.param("phoneContact", "123")
						.param("birthDate", "12/12/12")
						.param("country", "brazil")
						.param("aboutMe", "Hi")
						.param("currentAddress", "here")
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT"); // Set to PUT to match endpoint
							return request;
						}))
				.andExpect(status().isOk()); // Expect 200 for successful update
	}

	@Test
	public void testAddUserToGroup() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/addToGroup")
						.param("userId", "1")
						.param("groupId", "1"))
				.andExpect(status().isOk());
	}
}