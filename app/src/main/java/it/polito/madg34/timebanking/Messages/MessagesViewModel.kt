package it.polito.madg34.timebanking.Messages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import it.polito.madg34.timebanking.FirestoreRepository
import java.lang.Exception

class MessagesViewModel : ViewModel() {

    val currentUserMessages : MutableLiveData<List<Message>> =
        MutableLiveData<List<Message>>().also { loadMessages() }

    val allMessages : MutableLiveData<List<Message>> =
        MutableLiveData<List<Message>>().also { getAllUnreadMessages() }

    var otherUserEmail = ""
    var currentRelatedAdv = ""

    var receivedReqNumber = 0
    var sentNumber = 0

    private var listener1: ListenerRegistration? = null


    fun loadMessages() {
        if(!currentRelatedAdv.isNullOrEmpty()){
            listener1 = FirestoreRepository().getChatMessages(currentRelatedAdv).addSnapshotListener(
                EventListener { value, e ->
                    if (e != null) {
                        currentUserMessages.value = emptyList()
                        return@EventListener
                    }
                    if (value!!.documents.size > 0) {
                        currentUserMessages.value = value.documents.filter {
                            (it.getString("SENT_BY") == FirestoreRepository.currentUser.email &&
                                    it.getString("RECEIVED_BY") == otherUserEmail)
                                    ||
                                    (it.getString("SENT_BY") == otherUserEmail &&
                                            it.getString("RECEIVED_BY") == FirestoreRepository.currentUser.email)
                        }.mapNotNull { d -> d.toMessageObject() }
                        currentUserMessages.value = currentUserMessages.value?.sortedBy {
                            it.timeStamp
                        }
                        Log.d("EMAIL", currentUserMessages.value.toString())
                    }
                    else currentUserMessages.value = emptyList()
                })
        }
    }

    private fun getAllUnreadMessages() {
        FirestoreRepository().getAllMessages().addSnapshotListener(EventListener{ value, e ->
            if (e != null) {
                allMessages.value = emptyList()
                return@EventListener
            }
            if(value!!.documents.size > 0){
                allMessages.value = value.documents.mapNotNull { d -> d.toMessageObject() }
            }
        })
    }

    fun getCurrentUserMessages(): LiveData<List<Message>> {
        return currentUserMessages
    }

    fun getAllMessages() : LiveData<List<Message>>{
        return allMessages
    }

    fun sendNewMessage(messageContent: String): Task<Void> {
        val newMessage = Message("",
            messageContent, 0, FirestoreRepository.currentUser.email!!, otherUserEmail,
            System.currentTimeMillis(), currentRelatedAdv
        )

        return FirestoreRepository().setMessage(newMessage)
    }

    fun updateMessageRead(message : Message): Task<Void> {
        return FirestoreRepository().updateMessageReadDB(message.id)
    }

    private fun DocumentSnapshot.toMessageObject(): Message? {
        return try {
            val id = get("ID") as String
            val messageContent = get("MESSAGE_CONTENT") as String
            val read = get("READ") as Long
            val sentBy = get("SENT_BY") as String
            val received_by = get("RECEIVED_BY") as String
            val timestamp = get("TIMESTAMP") as Long
            val relatedAdv = get("RELATED_ADV") as String

            Message(id, messageContent, read.toInt(), sentBy, received_by, timestamp, relatedAdv)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}