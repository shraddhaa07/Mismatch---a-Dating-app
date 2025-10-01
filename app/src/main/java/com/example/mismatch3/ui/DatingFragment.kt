package com.example.mismatch3.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.mismatch3.R
import com.example.mismatch3.activity.MessageActivity
import com.example.mismatch3.adapter.DatingAdapter
import com.example.mismatch3.databinding.FragmentDatingBinding
import com.example.mismatch3.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class DatingFragment : Fragment() {

    private lateinit var binding: FragmentDatingBinding
    private lateinit var manager: CardStackLayoutManager
    private var list: ArrayList<UserModel> = arrayListOf()  // Initialize list here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatingBinding.inflate(inflater, container, false)

        // Initialize the CardStackLayoutManager here
        initCardStackLayoutManager()

        // Fetch data from Firebase
        getData()

        return binding.root
    }

    // Initialize the CardStackLayoutManager
    private fun initCardStackLayoutManager() {
        manager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                // Handle card dragging
                if (direction == Direction.Right) {
                    binding.cardStackView.setBackgroundColor(resources.getColor(R.color.swipe_right_green))
                } else if (direction == Direction.Left) {
                    binding.cardStackView.setBackgroundColor(resources.getColor(R.color.swipe_left_red))
                }else {
                    // Reset to default color if swiping in other directions
                    binding.cardStackView.setBackgroundColor(resources.getColor(android.R.color.transparent))
                }
            }

            override fun onCardSwiped(direction: Direction?) {
                if (manager.topPosition == list.size) {
                    Toast.makeText(requireContext(), "This is the last card", Toast.LENGTH_SHORT).show()
                } else if (direction == Direction.Right) {
                    // On right swipe, start the MessageActivity with the swiped user
                    val swipedUser = list[manager.topPosition - 1] // Adjusted for 0-indexing
                    openMessageActivity(swipedUser)
                }
            }

            override fun onCardRewound() {
                // Handle card rewind
            }

            override fun onCardCanceled() {
                // Handle card cancel
                // Reset the background color if the swipe is canceled
                binding.cardStackView.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }

            override fun onCardAppeared(view: View?, position: Int) {
                // Handle card appearance
            }

            override fun onCardDisappeared(view: View?, position: Int) {
                // Handle card disappearance
            }
        })

        manager.setVisibleCount(3)
        manager.setTranslationInterval(0.6f)
        manager.setScaleInterval(0.8f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)

        // Set up the CardStackView with the layout manager here
        binding.cardStackView.layoutManager = manager
        binding.cardStackView.itemAnimator = DefaultItemAnimator()
    }

    private fun getData() {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("SHUBH", "onDataChange:$snapshot")
                    if (snapshot.exists()) {
                        list.clear() // Clear the list before adding new data
                        for (data in snapshot.children) {
                            val model = data.getValue(UserModel::class.java)
                            if (model != null && model.number != FirebaseAuth.getInstance().currentUser?.phoneNumber) {
                                list.add(model)
                            }
                        }
                        list.shuffle()

                        // Set the adapter with the fetched data
                        binding.cardStackView.adapter = DatingAdapter(requireContext(), list)
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Function to open MessageActivity with the selected user
    private fun openMessageActivity(user: UserModel) {
        val intent = Intent(requireContext(), MessageActivity::class.java)
        intent.putExtra("userId", user.number)
        startActivity(intent)
    }
}
