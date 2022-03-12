package com.example.a359personalproject

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var categories:ArrayList<ListCategory> = ArrayList<ListCategory>()
    private var categoryNames:ArrayList<String> = ArrayList<String>()
    private var currentCategory: ListCategory = ListCategory("Filler")
    private var in_Text: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        //persistent data
//        val sharedPreferences:SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        //Runs if no saved data
        if(savedInstanceState == null) {
            //Default category
            categories.add(ListCategory("Main"))

            currentCategory = categories.get(0)
            //initialize
            initializeApp()
            //Initial fragment loaded first open
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer,
                    Fragment_main.newInstance(),
                    "FragmentMain"
                ).commit()
        }

        //Button to add new categories
        val addNewCatButton = findViewById<FloatingActionButton>(R.id.addCategoryButton)
        addNewCatButton.setOnClickListener {
            showInputBoxCat()
        }

        //Changes the data depending on selected spinner item
        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                val currentCategoryText = spinner.selectedItem.toString()
                for(i in categories) {
                    if (i.getCategoryName() == currentCategoryText) {
                        setCurrentCategory(i)
                    }
                }
                redraw()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Button to add new items on list
        val addNewItemButton = findViewById<FloatingActionButton>(R.id.addItemButton)
        addNewItemButton.setOnClickListener {
            showInputBox()
            redraw()
        }



    }

    //Resets the categories
    private fun initializeApp() {
        //Set up categories and drop down menu
        for (i in categories){
            categoryNames.add(i.getCategoryName())
        }

        val spinner = findViewById<Spinner>(R.id.spinner)
        val arrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }
    private fun clearAdapter(){
        val spinner = findViewById<Spinner>(R.id.spinner)
        val arrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        spinner.adapter = arrayAdapter
        arrayAdapter.clear()
        arrayAdapter.notifyDataSetChanged()
    }

    //Gets the current category to pass on to fragment
    public fun getCat(): ListCategory {
        return currentCategory
    }

    public fun setCurrentCategory(listItem: ListCategory){
        currentCategory = listItem
    }

    //Refreshes the fragment
    public fun redraw(){
        val frag = supportFragmentManager.findFragmentByTag("FragmentMain")
        supportFragmentManager
            .beginTransaction()
            .detach(supportFragmentManager.findFragmentByTag("FragmentMain")!!)
            .commit()
        supportFragmentManager
            .beginTransaction()
            .attach(supportFragmentManager.findFragmentByTag("FragmentMain")!!)
            .commit()
    }

    //Removes an item from list
    public fun removeItem(s : String){
        //Remove the item
        currentCategory.removeItem(s)

        //Clone to original copy
        for (i in categories.indices){
            if(categories.get(i).getCategoryName() == currentCategory.getCategoryName()) {
                categories[i] = currentCategory
                break
            }
        }
        redraw()
    }

    //Add new items
    fun showInputBox(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Title")

        val input = EditText(this)
        input.setHint("Enter text")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
            in_Text = input.text.toString()
            currentCategory.addItem(in_Text)
            Toast.makeText(applicationContext, "Item added", Toast.LENGTH_SHORT).show()
            redraw()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->

        })

        builder.show()
    }

    fun showInputBoxCat(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Title")

        val input = EditText(this)
        input.setHint("Enter text")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
            in_Text = input.text.toString()
            categories.add(ListCategory(in_Text))
            clearAdapter()
            initializeApp()
            Toast.makeText(applicationContext, "Category Added", Toast.LENGTH_SHORT).show()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->

        })

        builder.show()
    }

    //Persistent data
    private fun saveItemData(){

        val sharedPreferences:SharedPreferences = this.getSharedPreferences("itemsList", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        for(i in categories){
            var set = HashSet<String>()
            set = i.getItems().toHashSet()
            editor.putStringSet(i.getCategoryName(), set)
        }
    }

    private fun saveCategories(){
        val sharedPreferences:SharedPreferences = this.getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()
        var set = HashSet<String>()
        set = categoryNames.toHashSet()
        editor.putStringSet("Categories", set)
    }

    private fun loadCategoryData(){
        //Load category names and add them into an array
        val sharedPreferences:SharedPreferences = this.getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
        val savedString = sharedPreferences.getStringSet("Categories", null)
        categoryNames = ArrayList(savedString)
        for(i in categoryNames){
            categories.add(ListCategory(i))
        }
    }

    private fun loadCategoryItemsData(){
        //Load the items of each category into an array and insert into correct category
        val sharedPreferences:SharedPreferences = this.getSharedPreferences("itemsList", Context.MODE_PRIVATE)
        for(i in categoryNames){
            val savedItems = sharedPreferences.getStringSet(i, null)
            for(j in categories) if(j.getCategoryName() == i) {
                j.replaceItems(ArrayList(savedItems))
                break
            }
        }
    }
}