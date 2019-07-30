package com.example.e4app.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.e4app.R
import com.example.e4app.ui.wristband.wristbandActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_principal.*
import org.jetbrains.anko.toast

private const val PERMISSION_REQUEST = 10

class HomeActivity : AppCompatActivity() {

    private var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var name = edtName.text
        var comment = edtComment.text

        requestPermissions(permissions, PERMISSION_REQUEST)


        btnEmpezar.setOnClickListener {

            if(edtName.text.isEmpty() && edtComment.text.isEmpty()){
                toast("Complete los campos")
            }

            else{
                val intent: Intent = Intent(this, wristbandActivity::class.java )

                println("Nombre: " + name)
                println("Comentario: " + comment)

                intent.putExtra("Name", name.toString())
                intent.putExtra("Comment", comment.toString())
                startActivity(intent)

            }
        }
    }

    fun checkPermissions(context: Context, permissionsArray: Array<String>):Boolean{
        var allSuccess = true
        for (i in permissionsArray.indices){
            if(checkCallingOrSelfPermission(permissionsArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST){
            var allSuccess = true
            for (i in permissions.indices){
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    allSuccess = false
                    var requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain){
                        Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Ve a configuraciones y activa los permisos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
        }
    }
}
