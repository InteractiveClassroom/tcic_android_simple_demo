package com.example.tcic_android_simple_demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.qcloudclass.tcic.TCICConfig
import com.qcloudclass.tcic.TCICHeaderComponentConfig
import com.qcloudclass.tcic.TCICManager
import kotlinx.coroutines.*

class ClassroomSetupWizardActivity : AppCompatActivity() {
    companion object {
        private const val TOTAL_STEPS = 3
        private const val DOCUMENTATION_URL =
            "https://cloud.tencent.com/document/product/1639/79895#9b6257f6-95c7-4f5d-9eee-76edd86f80f7"
    }

    // UI Components
    private lateinit var step1Circle: View
    private lateinit var step2Circle: View
    private lateinit var step3Circle: View
    private lateinit var step1Text: TextView
    private lateinit var step2Text: TextView
    private lateinit var step3Text: TextView
    private lateinit var connector1: View
    private lateinit var connector2: View
    private lateinit var contentContainer: FrameLayout
    private lateinit var resetButton: ImageButton

    // Step 1 views
    private lateinit var secretKeyEditText: EditText
    private lateinit var secretIdEditText: EditText
    private lateinit var appIdEditText: EditText
    private lateinit var createUserButton: Button
    private lateinit var progressBar: ProgressBar

    // State
    private var currentStepIndex = 0
    private var isProcessing = false
    private var classroomInfo: ClassroomInfo? = null
    private val stepStatus = WizardStepStatus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_setup_wizard)

        initializeViews()
        updateStepIndicator()
        initTCICSDK()
        showStep(0)
    }

    private fun initTCICSDK() {
        TCICManager.initialize(this);
        TCICManager.setCallback(object : TCICManager.TCICCallback {
            override fun onJoinedClassSuccess() {
                runOnUiThread {
                    Toast.makeText(this@ClassroomSetupWizardActivity, "加入课堂成功", Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterExitedClass() {
                runOnUiThread {
                    Toast.makeText(this@ClassroomSetupWizardActivity, "已退出课堂，关闭页面", Toast.LENGTH_SHORT).show()
                    TCICManager.closeFlutterActivity() // 关闭当前 Activity
                }
            }

            override fun onJoinedClassFailed() {
                runOnUiThread {
                    Toast.makeText(this@ClassroomSetupWizardActivity, "加入课堂失败", Toast.LENGTH_SHORT).show()
                    TCICManager.closeFlutterActivity() // 关闭当前 Activity
                }
            }

            override fun onKickedOffClass() {
                runOnUiThread {
                    Toast.makeText(this@ClassroomSetupWizardActivity, "被踢出课堂", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onMemberJoinedClass(data: Map<*, *>) {
                runOnUiThread {
                    Toast.makeText(
                        this@ClassroomSetupWizardActivity,
                        "成员加入: $data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onMemberLeaveClass(data: Map<*, *>) {
                runOnUiThread {
                    Toast.makeText(
                        this@ClassroomSetupWizardActivity,
                        "成员离开: $data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onRecivedMessage(message: Map<*, *>) {
                runOnUiThread {
                    Toast.makeText(
                        this@ClassroomSetupWizardActivity,
                        "收到消息: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onError(errorCode: String, errorMsg: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@ClassroomSetupWizardActivity,
                        "错误: $errorMsg",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun initializeViews() {
        // Step indicators
        step1Circle = findViewById(R.id.step1_circle)
        step2Circle = findViewById(R.id.step2_circle)
        step3Circle = findViewById(R.id.step3_circle)
        step1Text = findViewById(R.id.step1_text)
        step2Text = findViewById(R.id.step2_text)
        step3Text = findViewById(R.id.step3_text)
        connector1 = findViewById(R.id.connector1)
        connector2 = findViewById(R.id.connector2)
        contentContainer = findViewById(R.id.content_container)
        resetButton = findViewById(R.id.reset_button)

        resetButton.setOnClickListener {
            if (!isProcessing && currentStepIndex > 0) {
                showResetConfirmDialog()
            }
        }
        resetButton.visibility = View.GONE
    }

    private fun updateStepIndicator() {
        // Update step 1
        updateStepAppearance(
            step1Circle,
            step1Text,
            0,
            stepStatus.isConfigurationCompleted
        )

        // Update step 2
        updateStepAppearance(
            step2Circle,
            step2Text,
            1,
            stepStatus.isClassroomCreated
        )

        // Update step 3
        updateStepAppearance(
            step3Circle,
            step3Text,
            2,
            stepStatus.isSetupCompleted
        )

        // Update connectors
        connector1.setBackgroundColor(
            if (currentStepIndex > 0 || stepStatus.isConfigurationCompleted)
                ContextCompat.getColor(this, R.color.green)
            else
                ContextCompat.getColor(this, R.color.gray_light)
        )

        connector2.setBackgroundColor(
            if (currentStepIndex > 1 || stepStatus.isClassroomCreated)
                ContextCompat.getColor(this, R.color.green)
            else
                ContextCompat.getColor(this, R.color.gray_light)
        )

        // Update reset button visibility
        resetButton.visibility = if (currentStepIndex > 0 && !isProcessing) View.VISIBLE else View.GONE
    }

    private fun updateStepAppearance(circle: View, text: TextView, stepIndex: Int, isCompleted: Boolean) {
        val isCurrentStep = currentStepIndex == stepIndex
        val isPassed = currentStepIndex > stepIndex || isCompleted

        circle.background = ContextCompat.getDrawable(
            this,
            when {
                isPassed -> R.drawable.circle_green
                isCurrentStep -> R.drawable.circle_blue
                else -> R.drawable.circle_gray
            }
        )

        text.setTextColor(
            ContextCompat.getColor(
                this,
                if (isPassed || isCurrentStep) R.color.black else R.color.gray_dark
            )
        )

        text.typeface = if (isPassed || isCurrentStep)
            android.graphics.Typeface.DEFAULT_BOLD
        else
            android.graphics.Typeface.DEFAULT
    }

    private fun showStep(stepIndex: Int) {
        contentContainer.removeAllViews()

        when (stepIndex) {
            0 -> showConfigurationStep()
            1 -> showClassroomCreationStep()
            2 -> showEnterClassroomStep()
        }
    }

    private fun showConfigurationStep() {
        val view = layoutInflater.inflate(R.layout.step_configuration, contentContainer, false)
        contentContainer.addView(view)

        secretKeyEditText = view.findViewById(R.id.secret_key_edit_text)
        secretIdEditText = view.findViewById(R.id.secret_id_edit_text)
        appIdEditText = view.findViewById(R.id.app_id_edit_text)
        createUserButton = view.findViewById(R.id.create_user_button)
        progressBar = view.findViewById(R.id.progress_bar)

        val documentationLink = view.findViewById<TextView>(R.id.documentation_link)
        documentationLink.setOnClickListener {
            openDocumentation()
        }

        createUserButton.setOnClickListener {
            handleConfiguration()
        }
    }

    private fun showClassroomCreationStep() {
        val view = layoutInflater.inflate(R.layout.step_classroom_creation, contentContainer, false)
        contentContainer.addView(view)

        val createClassroomButton = view.findViewById<Button>(R.id.create_classroom_button)
        val resetButton = view.findViewById<Button>(R.id.reset_button_step2)
        progressBar = view.findViewById(R.id.progress_bar)

        createClassroomButton.setOnClickListener {
            handleClassroomCreation()
        }

        resetButton.setOnClickListener {
            showResetConfirmDialog()
        }
    }

    private fun showEnterClassroomStep() {
        val view = layoutInflater.inflate(R.layout.step_enter_classroom, contentContainer, false)
        contentContainer.addView(view)

        val infoCard = view.findViewById<CardView>(R.id.info_card)
        val userIdText = view.findViewById<TextView>(R.id.user_id_text)
        val roomIdText = view.findViewById<TextView>(R.id.room_id_text)
        val enterClassroomButton = view.findViewById<Button>(R.id.enter_classroom_button)
        val resetButton = view.findViewById<Button>(R.id.reset_button_step3)

        classroomInfo?.let { info ->
            userIdText.text = "用户创建成功 ${info.userId}"
            roomIdText.text = "课堂创建成功 ${info.roomId}"
        }

        enterClassroomButton.setOnClickListener {
            handleEnterClassroom()
        }

        resetButton.setOnClickListener {
            showResetConfirmDialog()
        }
    }

    private fun handleConfiguration() {
        if (!validateConfigurationInputs()) return

        setProcessing(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                TCICCloudApi.setConfig(
                    secretId = secretIdEditText.text.toString(),
                    secretKey = secretKeyEditText.text.toString(),
                    appId = appIdEditText.text.toString().toInt()
                )

                val response = withContext(Dispatchers.IO) {
                    TCICCloudApi.registerUser()
                }

                if (response.hasError()) {
                    Log.e("error", response.errorMessage);
                    showErrorMessage("注册失败，${response.errorMessage}")
                    return@launch
                }

                stepStatus.isConfigurationCompleted = true
                currentStepIndex = 1

                classroomInfo = ClassroomInfo(
                    userId = response.userId,
                    token = response.token,
                    roomId = 0
                )

                updateStepIndicator()
                showStep(1)
                showSuccessMessage("用户注册成功!")

            } catch (e: Exception) {
                showErrorMessage("注册失败: ${e.message}")
            } finally {
                setProcessing(false)
            }
        }
    }

    private fun handleClassroomCreation() {
        val info = classroomInfo ?: return

        setProcessing(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    TCICCloudApi.createRoom(teacherId = info.userId)
                }

                if (response.hasError()) {
                    showErrorMessage("创建课堂失败，${response.errorMessage}")
                    return@launch
                }

                stepStatus.isClassroomCreated = true
                currentStepIndex = 2

                classroomInfo = info.copy(roomId = response.roomId)

                updateStepIndicator()
                showStep(2)
                showSuccessMessage("课堂创建成功！")

            } catch (e: Exception) {
                showErrorMessage("创建课堂失败: ${e.message}")
            } finally {
                setProcessing(false)
            }
        }
    }

    private fun handleEnterClassroom() {
        val info = classroomInfo ?: return

        val headerComponentConfig = TCICHeaderComponentConfig()
        headerComponentConfig.setHeaderLeftBuilder { HeaderLeftViewCreator() }

        val config = TCICConfig(
            info.token,
            info.roomId.toString(),
            info.userId,
            1 // Teacher role
        )
        config.headerComponentConfig = headerComponentConfig

        TCICManager.setConfig(config)
        val intent = TCICManager.getTCICIntent(this)
        startActivity(intent)
    }

    private fun validateConfigurationInputs(): Boolean {
        if (secretKeyEditText.text.isEmpty() ||
            secretIdEditText.text.isEmpty() ||
            appIdEditText.text.isEmpty()) {
            showErrorMessage("请填写完整的配置信息")
            return false
        }
        return true
    }

    private fun setProcessing(processing: Boolean) {
        isProcessing = processing
        progressBar.visibility = if (processing) View.VISIBLE else View.GONE

        when (currentStepIndex) {
            0 -> createUserButton.isEnabled = !processing
            1 -> {
                val createButton = findViewById<Button>(R.id.create_classroom_button)
                createButton?.isEnabled = !processing
            }
        }

        updateStepIndicator()
    }

    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认重置")
            .setMessage("这将清除所有配置信息并返回到第一步，确定要继续吗？")
            .setPositiveButton("确定") { _, _ ->
                resetWizard()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun resetWizard() {
        currentStepIndex = 0
        stepStatus.reset()
        isProcessing = false
        classroomInfo = null

        updateStepIndicator()
        showStep(0)

        showSuccessMessage("已重置到第一步")
    }

    private fun openDocumentation() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DOCUMENTATION_URL))
        startActivity(intent)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Data classes
data class ClassroomInfo(
    val userId: String,
    val token: String,
    val roomId: Int
)

class WizardStepStatus {
    var isConfigurationCompleted = false
    var isClassroomCreated = false
    var isSetupCompleted = false

    fun reset() {
        isConfigurationCompleted = false
        isClassroomCreated = false
        isSetupCompleted = false
    }
}