package com.polytron.remote
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
class MainActivity: AppCompatActivity(){
  private var ir: ConsumerIrManager? = null
  // NEC 38kHz Polytron 0x20DF family — ponytail: hardcoded NEC, add learn when model differs
  private fun necPattern(addr:Int, cmd:Int): IntArray{
    val out = mutableListOf<Int>()
    out.add(9000); out.add(4500)
    val data = ((addr and 0xFF) or ((addr.inv() and 0xFF) shl 8) or ((cmd and 0xFF) shl 16) or ((cmd.inv() and 0xFF) shl 24)).toLong() and 0xFFFFFFFFL
    for(i in 0 until 32){
      out.add(560)
      out.add(if(((data shr i) and 1L)==1L) 1690 else 560)
    }
    out.add(560)
    return out.toIntArray()
  }
  private val codes = mapOf(
    "power" to necPattern(0x20DF, 0x10),
    "mute" to necPattern(0x20DF, 0x90),
    "volp" to necPattern(0x20DF, 0x40),
    "volm" to necPattern(0x20DF, 0xC0),
    "chp" to necPattern(0x20DF, 0x00),
    "chm" to necPattern(0x20DF, 0x80),
    "source" to necPattern(0x20DF, 0xD0),
    "menu" to necPattern(0x20DF, 0x58),
    "exit" to necPattern(0x20DF, 0x5A),
    "up" to necPattern(0x20DF, 0x02),
    "down" to necPattern(0x20DF, 0x03),
    "left" to necPattern(0x20DF, 0x06),
    "right" to necPattern(0x20DF, 0x07),
    "ok" to necPattern(0x20DF, 0x0A),
    "0" to necPattern(0x20DF, 0x08),
    "1" to necPattern(0x20DF, 0x88),
    "2" to necPattern(0x20DF, 0x48),
    "3" to necPattern(0x20DF, 0xC8),
    "4" to necPattern(0x20DF, 0x28),
    "5" to necPattern(0x20DF, 0xA8),
    "6" to necPattern(0x20DF, 0x68),
    "7" to necPattern(0x20DF, 0xE8),
    "8" to necPattern(0x20DF, 0x18),
    "9" to necPattern(0x20DF, 0x98)
  )
  private fun send(key:String){
    val mgr = ir
    if(mgr==null || !mgr.hasIrEmitter()){ Toast.makeText(this,"HP tidak ada IR blaster",Toast.LENGTH_SHORT).show(); return }
    val pat = codes[key] ?: return
    try{ mgr.transmit(38000, pat); Toast.makeText(this,key,Toast.LENGTH_SHORT).show() }catch(e:Exception){ Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show() }
  }
  override fun onCreate(b:Bundle?){
    super.onCreate(b); setContentView(R.layout.activity_main)
    ir = getSystemService(CONSUMER_IR_SERVICE) as? ConsumerIrManager
    val warn = findViewById<TextView>(R.id.tWarn)
    if(ir==null || ir?.hasIrEmitter()==false){ warn.visibility=android.view.View.VISIBLE; warn.text="⚠ IR blaster tidak terdeteksi — test tetap bisa, tapi tidak akan memancar di HP tanpa IR" }
    fun btn(id:Int, key:String){ findViewById<MaterialButton>(id).setOnClickListener{ send(key) } }
    btn(R.id.bPower,"power"); btn(R.id.bMute,"mute"); btn(R.id.bVolP,"volp"); btn(R.id.bVolM,"volm"); btn(R.id.bChP,"chp"); btn(R.id.bChM,"chm")
    btn(R.id.bSource,"source"); btn(R.id.bMenu,"menu"); btn(R.id.bExit,"exit")
    btn(R.id.b0,"0"); btn(R.id.b1,"1"); btn(R.id.b2,"2"); btn(R.id.b3,"3"); btn(R.id.b4,"4"); btn(R.id.b5,"5"); btn(R.id.b6,"6"); btn(R.id.b7,"7"); btn(R.id.b8,"8"); btn(R.id.b9,"9")
    btn(R.id.bUp,"up"); btn(R.id.bDown,"down"); btn(R.id.bLeft,"left"); btn(R.id.bRight,"right"); btn(R.id.bOk,"ok")
  }
}
