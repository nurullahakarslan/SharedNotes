package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.folders.FolderListScreen
import com.example.myapplication.ui.screens.notes.NoteListScreen
import com.example.myapplication.ui.screens.notes.NoteDetailScreen
import com.example.myapplication.ui.screens.share.ShareManagementScreen

object Routes {
    const val LOGIN = "login"
    const val FOLDERS = "folders"
    const val NOTES = "notes/{folderId}/{folderName}"
    const val NOTE_DETAIL = "note_detail/{folderId}/{noteId}"
    const val SHARE = "share/{folderId}/{folderName}"

    fun notes(folderId: String, folderName: String) = "notes/$folderId/$folderName"
    fun noteDetail(folderId: String, noteId: String) = "note_detail/$folderId/$noteId"
    fun share(folderId: String, folderName: String) = "share/$folderId/$folderName"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()

    val startDestination = if (authRepository.getCurrentUserId() != null) Routes.FOLDERS else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.FOLDERS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FOLDERS) {
            FolderListScreen(
                onFolderClick = { folderId, folderName ->
                    navController.navigate(Routes.notes(folderId, folderName))
                },
                onShareClick = { folderId, folderName ->
                    navController.navigate(Routes.share(folderId, folderName))
                },
                onLogoutClick = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FOLDERS) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.NOTES,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            NoteListScreen(
                folderId = folderId,
                folderName = folderName,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Routes.noteDetail(folderId, noteId))
                },
                onCreateNoteClick = {
                    // Pass empty noteId for creation
                    navController.navigate(Routes.noteDetail(folderId, "new"))
                }
            )
        }

        composable(
            route = Routes.NOTE_DETAIL,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteDetailScreen(
                folderId = folderId,
                noteId = noteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SHARE,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            ShareManagementScreen(
                folderId = folderId,
                folderName = folderName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
