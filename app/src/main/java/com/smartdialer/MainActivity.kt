package com.smartdialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.GridLayout
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class MainActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private var number = StringBuilder()

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequest.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS))
        setContentView(createScreen())
    }

    private fun createScreen() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 24, 24, 14)
        setBackgroundColor(0xfff8f7ff.toInt())

        addView(TextView(context).apply {
            text = "SmartDialer"
            textSize = 30f
            setTextColor(0xff181621.toInt())
        })
        addView(TextView(context).apply {
            text = "Your calls, beautifully simple"
            textSize = 15f
            setTextColor(0xff686474.toInt())
        })
        addView(MaterialButton(context).apply {
            text = "Make SmartDialer your phone app"
            setOnClickListener { requestDefaultPhoneRole() }
        })
        addView(ruleCard())

        display = TextView(context).apply {
            text = "Enter a number"
            textSize = 27f
            gravity = Gravity.CENTER
            minHeight = 78
            setTextColor(0xff181621.toInt())
        }
        addView(display)

        val pad = GridLayout(context).apply { columnCount = 3; useDefaultMargins = true }
        listOf("1", "2\nABC", "3\nDEF", "4\nGHI", "5\nJKL", "6\nMNO",
            "7\nPQRS", "8\nTUV", "9\nWXYZ", "*", "0\n+", "#").forEach { key ->
            pad.addView(MaterialButton(context).apply {
                text = key
                textSize = 18f
                minHeight = 76
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    append(key.first())
                }
                setOnLongClickListener {
                    if (key.startsWith("0") && number.isEmpty()) append('+')
                    else clearLast()
                    true
                }
            })
        }
        addView(pad)

        addView(LinearLayout(context).apply {
            gravity = Gravity.CENTER
            addView(MaterialButton(context).apply {
                text = "⌫"
                setOnClickListener { clearLast() }
                setOnLongClickListener { number.clear(); updateDisplay(); true }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(MaterialButton(context).apply {
                text = "●  CALL"
                setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary))
                setBackgroundColor(0xff5b4bdb.toInt())
                setOnClickListener { placeCall() }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        })

        addView(MaterialCardView(context).apply {
            radius = 22f
            setCardBackgroundColor(0xffebe8ff.toInt())
            setContentPadding(18, 12, 18, 12)
            addView(TextView(context).apply {
                text = "Quick access\nFavorites  •  Recents  •  Contacts  •  Settings"
                textSize = 14f
                setTextColor(0xff4d4860.toInt())
            })
        })
    }

    private fun ruleCard() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(18, 12, 18, 12)
        setBackgroundColor(0xffebe8ff.toInt())
        val toggle = Switch(context).apply {
            text = "Ghost DND"
            textSize = 18f
            isChecked = RuleStore(this@MainActivity).silentEnabled
        }
        val target = EditText(context).apply {
            hint = "Number to silence"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(RuleStore(this@MainActivity).silentNumber)
        }
        toggle.setOnCheckedChangeListener { _, checked ->
            RuleStore(this@MainActivity).silentEnabled = checked
        }
        addView(toggle)
        addView(TextView(context).apply {
            text = "Uses Android call screening and keeps the call in history."
            setTextColor(0xff514d61.toInt())
        })
        addView(target)
        addView(MaterialButton(context).apply {
            text = "Save rule"
            setOnClickListener { RuleStore(this@MainActivity).silentNumber = target.text.toString() }
        })
    }

    private fun append(value: Char) {
        if (value in "0123456789*#" || (value == '+' && number.isEmpty())) {
            number.append(value)
            updateDisplay()
        }
    }

    private fun clearLast() {
        if (number.isNotEmpty()) number.deleteCharAt(number.lastIndex)
        updateDisplay()
    }

    private fun updateDisplay() {
        display.text = if (number.isEmpty()) "Enter a number" else number.toString()
    }

    private fun requestDefaultPhoneRole() {
        val roles = ContextCompat.getSystemService(this, RoleManager::class.java)
        if (roles?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
            startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        } else startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }

    private fun placeCall() {
        if (number.isEmpty()) return
        val telecom = getSystemService(TelecomManager::class.java)
        val uri = Uri.fromParts("tel", number.toString(), null)
        if (telecom != null && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            telecom.placeCall(uri, Bundle())
        } else startActivity(Intent(Intent.ACTION_DIAL, uri))
    }
}