package com.polytron.remote
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
class MainActivity: AppCompatActivity(){
  private var ir: ConsumerIrManager? = null
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
  // PLD 32T7511/S sniff HX1838 2026-09-11 addr 0x80 NEC — fix 2026-09-11 mute/source + dpad — ponytail: update lagi jika masih meleset
  private val codes = mapOf(
    "power" to listOf(necPattern(0x80, 0x17)),
    "mute" to listOf(necPattern(0x80, 0x15)),
    "source" to listOf(necPattern(0x80, 0x5C)),
    "av" to listOf(necPattern(0x80, 0x5C)),
    "1" to listOf(necPattern(0x80, 0x01)),
    "2" to listOf(necPattern(0x80, 0x02)),
    "3" to listOf(necPattern(0x80, 0x03)),
    "4" to listOf(necPattern(0x80, 0x04)),
    "5" to listOf(necPattern(0x80, 0x05)),
    "6" to listOf(necPattern(0x80, 0x06)),
    "7" to listOf(necPattern(0x80, 0x07)),
    "8" to listOf(necPattern(0x80, 0x08)),
    "9" to listOf(necPattern(0x80, 0x09)),
    "0" to listOf(necPattern(0x80, 0x00)),
    "volp" to listOf(necPattern(0x80, 0x50)),
    "volm" to listOf(necPattern(0x80, 0x51)),
    "chp" to listOf(necPattern(0x80, 0x12)),
    "chm" to listOf(necPattern(0x80, 0x18)),
    "menu" to listOf(necPattern(0x80, 0x0B)),
    "exit" to listOf(necPattern(0x80, 0x4B)),
    "up" to listOf(necPattern(0x80, 0x0C)),
    "down" to listOf(necPattern(0x80, 0x0D)),
    "left" to listOf(necPattern(0x80, 0x0E)),
    "right" to listOf(necPattern(0x80, 0x0F)),
    "ok" to listOf(necPattern(0x80, 0x14))
  )
  private fun send(key:String){
    val mgr = ir
    if(mgr==null || !mgr.hasIrEmitter()){ return }
    val pats = codes[key] ?: return
    try{
      for(pat in pats){ mgr.transmit(38000, pat); if(pats.size>1) Thread.sleep(120) }
    }catch(_:Exception){}
  }
  override fun onCreate(b:Bundle?){
    super.onCreate(b); setContentView(R.layout.activity_main)
    ir = getSystemService(CONSUMER_IR_SERVICE) as? ConsumerIrManager
    val warn = findViewById<TextView>(R.id.tWarn)
    val freq = ir?.carrierFrequencies
    if(ir==null || ir?.hasIrEmitter()==false){ warn.visibility=android.view.View.VISIBLE; warn.text="⚠ IR tidak terdeteksi" }
    else if(freq!=null){ warn.visibility=android.view.View.VISIBLE; warn.text="IR ready ${freq[0].minFrequency/1000}-${freq[0].maxFrequency/1000}kHz — arahkan 20cm ke sensor TV, lepas case jika tebal" }
    fun btn(id:Int, key:String){ findViewById<MaterialButton>(id).setOnClickListener{ send(key) } }
    btn(R.id.bPower,"power"); btn(R.id.bMute,"mute"); btn(R.id.bVolP,"volp"); btn(R.id.bVolM,"volm"); btn(R.id.bChP,"chp"); btn(R.id.bChM,"chm")
    btn(R.id.bSource,"source"); btn(R.id.bMenu,"menu"); btn(R.id.bExit,"exit")
    btn(R.id.b0,"0"); btn(R.id.b1,"1"); btn(R.id.b2,"2"); btn(R.id.b3,"3"); btn(R.id.b4,"4"); btn(R.id.b5,"5"); btn(R.id.b6,"6"); btn(R.id.b7,"7"); btn(R.id.b8,"8"); btn(R.id.b9,"9")
    btn(R.id.bUp,"up"); btn(R.id.bDown,"down"); btn(R.id.bLeft,"left"); btn(R.id.bRight,"right"); btn(R.id.bOk,"ok")
  }
}
