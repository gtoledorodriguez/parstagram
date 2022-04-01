package com.gladystoledo.parstagram.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gladystoledo.parstagram.MainActivity
import com.gladystoledo.parstagram.Post
import com.gladystoledo.parstagram.PostAdapter
import com.gladystoledo.parstagram.R
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery

open class FeedFragment : Fragment() {

    lateinit var postsRecyclerView: RecyclerView
    lateinit var adapter: PostAdapter

    var allPosts: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsRecyclerView = view.findViewById(R.id.postRecyclerView)
        //Create Layout for each row in list
        //Create data sours for each row (this is the Post class)
        //Create adapter that will bridge data and row layout (PostAdapter Class)
        //set adapter on Reycler ver
        adapter = PostAdapter(requireContext(), allPosts)
        postsRecyclerView.adapter = adapter

        //set layout manger on reyclerview
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        queryPosts()
    }

    //Query for all posts in our server
    open fun queryPosts() {
        // Specify which class to query
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)

        query.include(Post.KEY_USER)
        //return posts in descending order: newer post appear first
        query.addDescendingOrder("createdAt")

        //Only return the most recent 20 posts
        query.setLimit(20)
        query.findInBackground(object: FindCallback<Post> {
            //Find all Post Objects
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null){
                    //Something has gone wrong
                    Log.e(TAG, "Error fetching posts")
                }else{
                    if (posts != null){
                        for (post in posts){
                            Log.i(
                                TAG, "Post: " + post.getDescription() + ", username: "
                                    + post.getUser()?.username)
                        }
                        allPosts.addAll(posts)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    companion object{
        const val TAG = "FeedFragment"
    }

}