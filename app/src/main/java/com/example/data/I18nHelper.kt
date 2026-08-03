package com.example.data

object I18nHelper {
    private val translations = mapOf(
        "en" to mapOf(
            "pipeline" to "Pipeline",
            "prep_hub" to "Prep Hub",
            "community" to "Community",
            "blog" to "Blog",
            "referrals" to "Referrals",
            "tools" to "Tools & AI",
            "profile" to "Profile",
            "job_tracker" to "Job Applications Pipeline",
            "add_job" to "Add Job",
            "question_bank" to "Question Bank",
            "quizzes" to "Practice Quizzes",
            "mock_interviews" to "Mock Interviews",
            "community_feed" to "Community Feed",
            "rewards" to "Rewards & Wallet",
            "resume_analyzer" to "AI Resume Analyzer",
            "offline_status" to "Synced • Offline Ready",
            "daily_challenge" to "Daily Interview Challenge"
        ),
        "hi" to mapOf(
            "pipeline" to "पाइपलाइन",
            "prep_hub" to "तैयारी केंद्र",
            "community" to "समुदाय",
            "blog" to "ब्लॉग",
            "tools" to "टूल और एआई",
            "profile" to "प्रोफ़ाइल",
            "job_tracker" to "नौकरी आवेदन पाइपलाइन",
            "add_job" to "नौकरी जोड़ें",
            "question_bank" to "प्रश्न बैंक",
            "quizzes" to "अभ्यास प्रश्नोत्तरी",
            "mock_interviews" to "मॉक साक्षात्कार",
            "community_feed" to "समुदाय फीड",
            "rewards" to "पुरस्कार और वॉलेट",
            "resume_analyzer" to "एआई रेज़्यूमे विश्लेषक",
            "offline_status" to "सिंक किया गया • ऑफ़लाइन तैयार",
            "daily_challenge" to "दैनिक साक्षात्कार चुनौती"
        ),
        "mr" to mapOf(
            "pipeline" to "पायपलाईन",
            "prep_hub" to "तयारी केंद्र",
            "community" to "समुदाय",
            "blog" to "ब्लॉग",
            "tools" to "टूल्स आणि AI",
            "profile" to "प्रोफाईल",
            "job_tracker" to "नोकरी अर्ज पायपलाईन",
            "add_job" to "नोकरी जोडा",
            "question_bank" to "प्रश्न संच",
            "quizzes" to "सराव चाचण्या",
            "mock_interviews" to "मॉक मुलाखती",
            "community_feed" to "समुदाय फीड",
            "rewards" to "बक्षीस आणि वॉलेट",
            "resume_analyzer" to "AI रेझ्युमे विश्लेषक",
            "offline_status" to "सिंक केलेले • ऑफलाइन तयार",
            "daily_challenge" to "दैनिक मुलाखत आव्हान"
        )
    )

    fun getString(key: String, lang: String = "en"): String {
        return translations[lang]?.get(key)
            ?: translations["en"]?.get(key)
            ?: key
    }
}
