package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.Objects


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        setupAdapter(emptyList())
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar
        btnConfirmar.setOnClickListener{
        val nomeInserido = nomeUsuario.text.toString()
            saveUserLocal(nomeInserido)
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(nome: String) {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val userLocal = getPreferences(Context.MODE_PRIVATE) ?: return
        with(userLocal.edit()) {
            putString(getString(R.string.nome_usuario), nome)
            apply()
        }
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario, exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo

        val userName = getPreferences(Context.MODE_PRIVATE)
        val savedName = userName.getString(getString(R.string.nome_usuario), "")
        nomeUsuario.setText(savedName)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso

        val userName = nomeUsuario.text.toString()
        githubApi.getAllRepositoriesByUser(userName).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if(response.isSuccessful) {
                    val repositories = response.body() ?: emptyList()
                    setupAdapter(repositories)
                }

            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {

            }

        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */

        val adapter = RepositoryAdapter(list)
        listaRepositories.adapter = adapter
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}