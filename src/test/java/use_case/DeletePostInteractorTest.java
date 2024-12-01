package use_case;

import daos.DBUserDataAccessObject;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import use_case.delete_post.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeletePostInteractorTest {

    private DeletePostDataAccessInterface mockPostDB;
    private DeletePostOutputBoundary mockPresenter;
    private DeletePostInteractor interactor;
    private DBUserDataAccessObject mockUserRepo;

    @BeforeEach
    void setUp() {
        mockPostDB = Mockito.mock(DeletePostDataAccessInterface.class);
        mockPresenter = Mockito.mock(DeletePostOutputBoundary.class);
        mockUserRepo = Mockito.mock(DBUserDataAccessObject.class);
        interactor = new DeletePostInteractor(mockPostDB, mockPresenter, mockUserRepo);
    }

    private User createMockUser(String userId) {
        return createMockUser(userId, new ArrayList<>());
    }

    private User createMockUser(String userId, List<String> moderatingPosts) {
        User mockUser = Mockito.mock(User.class);
        when(mockUser.getUserID()).thenReturn(userId);
        when(mockUser.getModerating()).thenReturn(moderatingPosts);
        return mockUser;
    }

    @Test
    void deletePostSuccessTest() {
        String postId = "post123";
        String userId = "user123";

        User mockUser = createMockUser(userId);
        when(mockUserRepo.getCurrentUser()).thenReturn(mockUser);
        when(mockPostDB.existsByID(postId)).thenReturn(true);
        when(mockPostDB.getPostAuthorId(postId)).thenReturn(userId);

        DeletePostInputData inputData = new DeletePostInputData(postId, userId, userId);

        interactor.deletePost(inputData);

        verify(mockPostDB).deletePost(postId);
        ArgumentCaptor<DeletePostOutputData> outputCaptor = ArgumentCaptor.forClass(DeletePostOutputData.class);
        verify(mockPresenter).prepareSuccessView(outputCaptor.capture());

        DeletePostOutputData capturedOutput = outputCaptor.getValue();
        assertEquals(postId, capturedOutput.getPostId());
        assertTrue(capturedOutput.isDeletionSuccessful());
    }

    @Test
    void deletePostFailsWhenPostDoesNotExistTest() {
        String postId = "post123";
        String userId = "user123";

        User mockUser = createMockUser(userId);
        when(mockUserRepo.getCurrentUser()).thenReturn(mockUser);
        when(mockPostDB.existsByID(postId)).thenReturn(false);

        DeletePostInputData inputData = new DeletePostInputData(postId, userId, userId);

        DeletePostFailedException exception = assertThrows(DeletePostFailedException.class,
                () -> interactor.deletePost(inputData));
        assertEquals("Post with given ID doesn't exist.", exception.getMessage());

        verify(mockPresenter).prepareFailView("Post with given ID doesn't exist.");
        verify(mockPostDB, never()).deletePost(anyString());
    }

    @Test
    void deletePostFailsWhenUserUnauthorizedTest() {
        String postId = "post123";
        String userId = "user123";

        User mockUser = createMockUser("otherUser"); // Different user
        when(mockUserRepo.getCurrentUser()).thenReturn(mockUser);
        when(mockPostDB.existsByID(postId)).thenReturn(true);
        when(mockPostDB.getPostAuthorId(postId)).thenReturn(userId); // Original author's ID

        DeletePostInputData inputData = new DeletePostInputData(postId, "otherUser", userId);

        DeletePostFailedException exception = assertThrows(DeletePostFailedException.class,
                () -> interactor.deletePost(inputData));
        assertEquals("User does not have permission to delete this post.", exception.getMessage());

        verify(mockPresenter).prepareFailView("User does not have permission to delete this post.");
        verify(mockPostDB, never()).deletePost(anyString());
    }

    @Test
    void deletePostFailsOnExceptionTest() {
        String postId = "post123";
        String userId = "user123";

        User mockUser = createMockUser(userId);
        when(mockUserRepo.getCurrentUser()).thenReturn(mockUser);
        when(mockPostDB.existsByID(postId)).thenReturn(true);
        when(mockPostDB.getPostAuthorId(postId)).thenReturn(userId);
        doThrow(new RuntimeException("DB error")).when(mockPostDB).deletePost(postId);

        DeletePostInputData inputData = new DeletePostInputData(postId, userId, userId);

        DeletePostFailedException exception = assertThrows(DeletePostFailedException.class,
                () -> interactor.deletePost(inputData));
        assertEquals("Failed to delete the post.", exception.getMessage());

        verify(mockPresenter).prepareFailView("Failed to delete the post.");
        verify(mockPostDB).deletePost(postId);
    }

    @Test
    void switchToHomePageViewTest() {
        interactor.switchToHomePageView();
        verify(mockPresenter).switchToHomePageView();
    }

    @Test
    void getAuthorIdTest() {
        String postId = "post123";
        String expectedAuthorId = "author123";
        when(mockPostDB.getPostAuthorId(postId)).thenReturn(expectedAuthorId);

        String actualAuthorId = interactor.getAuthorId(postId);

        assertEquals(expectedAuthorId, actualAuthorId);
        verify(mockPostDB).getPostAuthorId(postId);
    }
}
