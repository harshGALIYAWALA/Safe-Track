package com.example.android.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.android.databinding.ContactItemBinding
import com.example.android.model.ContactModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ContactAdapter(
    private val contactList: ArrayList<ContactModel>
): RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactViewHolder {
        return ContactViewHolder(ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: ContactViewHolder,
        position: Int
    ) {
        val items = contactList[position]
        holder.bind(items)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    inner class ContactViewHolder(private val binding: ContactItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(items: ContactModel) {
//
            binding.nameContactItem.text = items.name
            binding.numberContactItem.text = items.number
            binding.deleteContactItem.setOnClickListener{
                deleteContact(items)
            }


            // Load contact image
//            if(items.image != null){
//                Glide.with(binding.imageContactitem.context)
//                    .load(items.image)
//                    .into(binding.imageContactitem)
//            }
        }

        private fun deleteContact(model: ContactModel) {

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val key = model.key

            if (userId != null && key != null) {
                val contactRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("contacts").child(key)
                contactRef.removeValue().addOnSuccessListener{
                    val index = contactList.indexOf(model)
                    if (index != -1){
                        contactList.removeAt(index)
                        notifyItemRemoved(index)
                    }
                }.addOnFailureListener{
                    Toast.makeText(binding.root.context, "Failed to delete contact", Toast.LENGTH_SHORT).show()
                }

            }
        }




    }


}

