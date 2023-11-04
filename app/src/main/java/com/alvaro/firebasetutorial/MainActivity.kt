package com.alvaro.firebasetutorial


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alvaro.firebasetutorial.data.FirebaseInstance
import com.alvaro.firebasetutorial.domain.Actions
import com.alvaro.firebasetutorial.domain.Todo
import com.alvaro.firebasetutorial.ui.theme.FirebaseTutorialTheme
import com.alvaro.firebasetutorial.ui.theme.Purple80


class MainActivity : ComponentActivity() {

    private lateinit var firebaseInstance: FirebaseInstance

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            firebaseInstance = FirebaseInstance(this)
            FirebaseTutorialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showDialog by remember {
                        mutableStateOf(false)
                    }

                    Scaffold(topBar = { TodoTopAppBar(onclick = { showDialog = !showDialog }) }) {
                        RealtimeBasic(
                            firebaseInstance = firebaseInstance,
                            modifier = Modifier.padding(it),
                            showDialog = showDialog
                        ) { showDialog = false }
                    }

                }
            }
        }
    }
}


@Composable
fun RealtimeBasic(
    firebaseInstance: FirebaseInstance,
    modifier: Modifier = Modifier,
    showDialog: Boolean,
    onDismissRequest: () -> Unit
) {

    var listTodos by remember {
        mutableStateOf(listOf<Pair<String, Todo>>())
    }

    val listState = rememberLazyListState()

    LaunchedEffect(key1 = "key1") {
        firebaseInstance.getData().collect {
            listTodos = it
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        addTaskTodo(showDialog, onDismissRequest, onclick = { title, description ->
            firebaseInstance.writeOnFirebase(title, description)
            onDismissRequest()

        })

        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 64.dp)) {
            items(listTodos, key = { it.first }) { item ->
                RealtimeBasicListItem(
                    title = item.second.title!!,
                    description = item.second.description!!,
                    reference = item.first,
                    done = item.second.done!!
                ) { actions, ref ->
                    when (actions) {
                        Actions.DELETE -> firebaseInstance.removeFromDatabase(ref)
                        Actions.DONE -> firebaseInstance.updateFromDatabase(ref)
                    }
                }
            }
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopAppBar(onclick: () -> Unit) {


    TopAppBar(
        title = { Text(text = "Realtime Database Basic") },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple80), actions = {
            IconButton(onClick = {
                onclick()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addTaskTodo(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onclick: (String, String) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    if (showDialog) {
        Dialog(onDismissRequest = onDismissRequest, DialogProperties(usePlatformDefaultWidth = false)) {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Añade tarea",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 22.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), label = { Text(text = "Title") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Purple80,
                        focusedLabelColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), label = { Text(text = "Description") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Purple80,
                        focusedLabelColor = Color.Black,
                    ), maxLines = 4
                )
                Button(
                    onClick = {
                        if (title.isEmpty() || description.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Campos no pueden estar vacios",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onclick(title, description)

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(text = "Añadir Tarea")
                }
            }

        }
    }
}


@Composable
fun RealtimeBasicListItem(
    title: String,
    description: String,
    reference: String,
    done: Boolean,
    onclick: (Actions, String) -> Unit,

    ) {

    Log.i("DoneValue", done.toString())
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, color = if (done) Color.Green else Purple80),
        colors = CardDefaults.cardColors(containerColor = Purple80)
    ) {

        Row(Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                )
                Text(
                    text = description, modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = reference,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column {
                IconButton(onClick = { onclick(Actions.DONE, reference) }) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null)
                }

                IconButton(onClick = { onclick(Actions.DELETE, reference) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }

    }

}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    val context = LocalContext.current
//    val firebaseInstance = FirebaseInstance(context)
//    RealtimeBasic(onclick = {}, firebaseInstance)
//}
/*

@Preview(showBackground = true)
@Composable
fun RealtimeBasicListItemPreview() {
    RealtimeBasicListItem(
        title = "Hola",
        description = "Mundo",
        reference = "reference",
        onclick = {})
}*/
