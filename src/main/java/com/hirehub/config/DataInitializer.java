package com.hirehub.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hirehub.model.Company;
import com.hirehub.model.Interview;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.Notification;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.SavedJob;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.InterviewRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.NotificationRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.util.PasswordUtil;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final InterviewRepository interviewRepository;
    private final NotificationRepository notificationRepository;
    private final ResumeRecordRepository resumeRecordRepository;

    public DataInitializer(UserAccountRepository accountRepository,
                           UserProfileRepository profileRepository,
                           CompanyRepository companyRepository,
                           JobRepository jobRepository,
                           JobApplicationRepository applicationRepository,
                           SavedJobRepository savedJobRepository,
                           InterviewRepository interviewRepository,
                           NotificationRepository notificationRepository,
                           ResumeRecordRepository resumeRecordRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.interviewRepository = interviewRepository;
        this.notificationRepository = notificationRepository;
        this.resumeRecordRepository = resumeRecordRepository;
    }

    @Override
    public void run(String... args) {
        // Initialize Default Admin Account if not present
        if (!accountRepository.existsById("admin") && !accountRepository.existsById("admin@hirehub.com")) {
            String adminHash = PasswordUtil.hash("admin123");
            UserAccount admin = new UserAccount("admin@hirehub.com", adminHash, "System Administrator", "Admin", "+91 9876543210", "Bengaluru, India", "Master of Computer Applications", 2020);
            accountRepository.save(admin);

            UserAccount adminAlias = new UserAccount("admin", adminHash, "Admin Superuser", "Admin", "+91 9876543210", "Bengaluru, India", "Admin", 2020);
            accountRepository.save(adminAlias);
        }

        // Initialize sample recruiters and companies if none exist
        if (companyRepository.count() == 0) {
            String defaultPassword = PasswordUtil.hash("recruiter123");

            // Recruiter 1: Google
            UserAccount r1 = new UserAccount("recruiter.google@hirehub.com", defaultPassword, "Sundar Recruiter", "Recruiter", "+91 9811122233", "Bengaluru, India", "MBA HR", 2018);
            accountRepository.save(r1);
            Company c1 = new Company("Google India", "https://careers.google.com", "Tier-1 Tech Leader", "Technology & Internet", "Bengaluru, India", "recruiter.google@hirehub.com", "Google's mission is to organize the world's information and make it universally accessible and useful.");
            companyRepository.save(c1);

            // Recruiter 2: Microsoft
            UserAccount r2 = new UserAccount("recruiter.msft@hirehub.com", defaultPassword, "Satya Talent Lead", "Recruiter", "+91 9822233344", "Hyderabad, India", "MS HR Management", 2017);
            accountRepository.save(r2);
            Company c2 = new Company("Microsoft IDC", "https://careers.microsoft.com", "Global Cloud & Software Giant", "Cloud Computing & Software", "Hyderabad, India", "recruiter.msft@hirehub.com", "Empowering every person and every organization on the planet to achieve more.");
            companyRepository.save(c2);

            // Recruiter 3: Amazon
            UserAccount r3 = new UserAccount("recruiter.amazon@hirehub.com", defaultPassword, "Andy Talent Acquisition", "Recruiter", "+91 9833344455", "Bengaluru, India", "MBA", 2019);
            accountRepository.save(r3);
            Company c3 = new Company("Amazon AWS", "https://amazon.jobs", "Global E-Commerce & Cloud Leader", "Cloud & E-Commerce", "Bengaluru, India", "recruiter.amazon@hirehub.com", "Earth's most customer-centric company and pioneer of AWS cloud infrastructure.");
            companyRepository.save(c3);

            // Common demo recruiter
            UserAccount rDemo = new UserAccount("recruiter@hirehub.com", defaultPassword, "HireHub Primary Recruiter", "Recruiter", "+91 9800011122", "Bengaluru, India", "HR Director", 2016);
            accountRepository.save(rDemo);
        }

        // Initialize candidate profiles if none exist
        if (profileRepository.count() == 0) {
            String candidatePassword = PasswordUtil.hash("candidate123");

            // Candidate 1: Alex Johnson (Full Stack Java & Spring Boot)
            UserAccount u1 = new UserAccount("alex.johnson@example.com", candidatePassword, "Alex Johnson", "Student", "+91 9876540001", "Bengaluru, India", "B.Tech Computer Science", 2024);
            accountRepository.save(u1);
            UserProfile p1 = new UserProfile();
            p1.setEmail("alex.johnson@example.com");
            p1.setName("Alex Johnson");
            p1.setPhone("+91 9876540001");
            p1.setRole("Student");
            p1.setLocation("Bengaluru, India");
            p1.setDegree("B.Tech in Computer Science & Engineering");
            p1.setCollege("National Institute of Technology (NIT)");
            p1.setSpecialization("Software Engineering");
            p1.setGraduationYear(2024);
            p1.setCgpa("8.9 / 10.0");
            p1.setPreferredRole("Full Stack Java Developer");
            p1.setPreferredLocation("Bengaluru");
            p1.setExperienceLevel("Fresher / 0-1 Year");
            p1.setSkills("Java, Spring Boot, MySQL, REST API, HTML5, CSS3, JavaScript, Microservices, Git");
            p1.setSummary("Passionate full-stack Java developer with hands-on expertise building enterprise Spring Boot microservices, REST APIs, and modern responsive web interfaces.");
            p1.setProjects("HireHub Recruitment Portal, Microservices E-Commerce Platform, Realtime Chat Application");
            p1.setExperienceDetails("Software Engineering Intern at TechSolutions (6 months): Built RESTful microservices using Spring Boot & MySQL.");
            p1.setEducationDetails("B.Tech Computer Science (2020 - 2024), CGPA: 8.9. High Distinction.");
            profileRepository.save(p1);

            // Also create shortcut candidate login: candidate@hirehub.com
            UserAccount uDemo = new UserAccount("candidate@hirehub.com", candidatePassword, "Demo Candidate", "Student", "+91 9876540000", "Bengaluru, India", "B.Tech Computer Science", 2024);
            accountRepository.save(uDemo);
            UserProfile pDemo = new UserProfile();
            pDemo.setEmail("candidate@hirehub.com");
            pDemo.setName("Demo Candidate");
            pDemo.setPhone("+91 9876540000");
            pDemo.setRole("Student");
            pDemo.setLocation("Bengaluru, India");
            pDemo.setDegree("B.Tech Computer Science");
            pDemo.setCollege("Indian Institute of Technology (IIT)");
            pDemo.setSpecialization("Computer Science");
            pDemo.setGraduationYear(2024);
            pDemo.setCgpa("9.1 / 10.0");
            pDemo.setPreferredRole("Full Stack Java Developer");
            pDemo.setPreferredLocation("Bengaluru");
            pDemo.setExperienceLevel("Fresher / 0-1 Year");
            pDemo.setSkills("Java, Spring Boot, MySQL, HTML5, CSS3, JavaScript, Python, SQL, REST API, Git");
            pDemo.setSummary("Proactive software engineer skilled in Java, Spring Boot, databases, and web technologies seeking high-impact developer roles.");
            pDemo.setProjects("AI-Powered Job Portal, Smart Parking IoT System");
            profileRepository.save(pDemo);

            // Candidate 2: Priya Sharma (AI / ML & Data Science)
            UserAccount u2 = new UserAccount("priya.sharma@example.com", candidatePassword, "Priya Sharma", "Student", "+91 9876540002", "Hyderabad, India", "M.S. Data Science", 2023);
            accountRepository.save(u2);
            UserProfile p2 = new UserProfile();
            p2.setEmail("priya.sharma@example.com");
            p2.setName("Priya Sharma");
            p2.setPhone("+91 9876540002");
            p2.setRole("Student");
            p2.setLocation("Hyderabad, India");
            p2.setDegree("M.S. in Data Science & Machine Learning");
            p2.setCollege("International Institute of Information Technology (IIIT)");
            p2.setGraduationYear(2023);
            p2.setCgpa("9.3 / 10.0");
            p2.setPreferredRole("AI / Machine Learning Engineer");
            p2.setSkills("Python, Machine Learning, Deep Learning, SQL, PyTorch, Pandas, Scikit-Learn, TensorFlow, NLP");
            p2.setSummary("Data scientist and ML engineer experienced in building predictive models, NLP classifiers, and scalable deep learning pipelines.");
            profileRepository.save(p2);

            // Candidate 3: Rahul Verma (Cloud & DevOps)
            UserAccount u3 = new UserAccount("rahul.verma@example.com", candidatePassword, "Rahul Verma", "Student", "+91 9876540003", "Pune, India", "B.E. Information Technology", 2024);
            accountRepository.save(u3);
            UserProfile p3 = new UserProfile();
            p3.setEmail("rahul.verma@example.com");
            p3.setName("Rahul Verma");
            p3.setPhone("+91 9876540003");
            p3.setRole("Student");
            p3.setLocation("Pune, India");
            p3.setDegree("B.E. Information Technology");
            p3.setCollege("Pune Institute of Computer Technology");
            p3.setGraduationYear(2024);
            p3.setCgpa("8.6 / 10.0");
            p3.setPreferredRole("Cloud DevOps Engineer");
            p3.setSkills("AWS, Docker, Kubernetes, Linux, CI/CD, Terraform, Python, Bash, Git");
            p3.setSummary("DevOps and Cloud enthusiast with practical knowledge of containerization, CI/CD pipelines, and cloud automation on AWS.");
            profileRepository.save(p3);

            // Candidate 4: Emily Davis (Frontend & UI/UX)
            UserAccount u4 = new UserAccount("emily.davis@example.com", candidatePassword, "Emily Davis", "Student", "+91 9876540004", "Remote", "B.Des Digital Media", 2024);
            accountRepository.save(u4);
            UserProfile p4 = new UserProfile();
            p4.setEmail("emily.davis@example.com");
            p4.setName("Emily Davis");
            p4.setPhone("+91 9876540004");
            p4.setRole("Student");
            p4.setLocation("Remote");
            p4.setDegree("B.Des in Interaction Design");
            p4.setCollege("National Institute of Design");
            p4.setGraduationYear(2024);
            p4.setCgpa("9.0 / 10.0");
            p4.setPreferredRole("UI/UX Designer & Frontend Developer");
            p4.setSkills("Figma, UI/UX Design, HTML5, CSS3, JavaScript, Bootstrap, React, Wireframing");
            p4.setSummary("Creative UI/UX Designer and Frontend Specialist building intuitive user interfaces, wireframes, and responsive web experiences.");
            profileRepository.save(p4);
        }

        // Initialize sample jobs and internships if none exist
        if (jobRepository.count() == 0) {
            String gEmail = "recruiter.google@hirehub.com";
            String mEmail = "recruiter.msft@hirehub.com";
            String aEmail = "recruiter.amazon@hirehub.com";

            // --- 10+ JOBS ---
            Job j1 = new Job("Full Stack Java Developer", "Google India", "Software Development", "Bengaluru, India", "Full-time", "Hybrid", "Fresher / 0-2 Years", "₹14,00,000 - ₹22,00,000 / year", "Java, Spring Boot, MySQL, REST API, JavaScript, Microservices", "Join Google's core product engineering team building high-performance cloud applications.", gEmail);
            j1.setJobCode("HH-JOB-1001");
            j1.setResponsibilities("Design, develop, and maintain high-throughput REST APIs and microservices. Collaborate with cross-functional teams to deliver scalable software solutions.");
            j1.setRequirements("Strong proficiency in Java 17+, Spring Boot framework, SQL databases, and fundamentals of data structures & algorithms.");
            j1.setBenefits("Health Insurance, Annual Learning Allowance, Stock Options, Free Meals & Transport, Flexible Work Hours");
            j1.setDeadline("2026-12-31");
            j1.setVacancies(5);
            jobRepository.save(j1);

            Job j2 = new Job("AI / Machine Learning Engineer", "Microsoft IDC", "Artificial Intelligence", "Hyderabad, India", "Full-time", "On-site", "1-3 Years", "₹18,00,000 - ₹28,00,000 / year", "Python, Machine Learning, PyTorch, Deep Learning, SQL, NLP", "Build next-generation AI foundation models and intelligent copilot services at Microsoft.", mEmail);
            j2.setJobCode("HH-JOB-1002");
            j2.setResponsibilities("Develop scalable machine learning models, fine-tune neural architectures, and integrate AI inference pipelines into cloud services.");
            j2.setRequirements("Hands-on experience with Python, PyTorch/TensorFlow, model training, evaluation metrics, and large-scale data preprocessing.");
            j2.setBenefits("Comprehensive Medical Insurance, Relocation Assistance, Wellness Allowance, Microsoft Surface Gear");
            j2.setDeadline("2026-11-30");
            j2.setVacancies(3);
            jobRepository.save(j2);

            Job j3 = new Job("Cloud DevOps Engineer", "Amazon AWS", "Cloud Computing", "Bengaluru, India", "Full-time", "Remote", "1-3 Years", "₹16,00,000 - ₹25,00,000 / year", "AWS, Docker, Kubernetes, Linux, CI/CD, Terraform, Python", "Help manage AWS infrastructure at scale and build resilient automated deployment pipelines.", aEmail);
            j3.setJobCode("HH-JOB-1003");
            j3.setResponsibilities("Architect, automate, and maintain cloud infrastructure across multi-region AWS environments. Enhance CI/CD pipelines and infrastructure observability.");
            j3.setRequirements("Experience with AWS services (EC2, ECS, EKS, S3, IAM), Docker, Kubernetes orchestration, and Infrastructure as Code.");
            j3.setBenefits("Remote Work Stipend, Health & Life Insurance, Amazon Discounts, Annual Bonuses");
            j3.setDeadline("2026-12-15");
            j3.setVacancies(4);
            jobRepository.save(j3);

            Job j4 = new Job("Senior Data Scientist", "Google India", "Data Science", "Bengaluru, India", "Full-time", "Hybrid", "3-5 Years", "₹20,00,000 - ₹30,00,000 / year", "Python, SQL, Machine Learning, Data Science, Pandas, Statistics", "Deliver predictive insights, experimentation analysis, and data-driven recommendations.", gEmail);
            j4.setJobCode("HH-JOB-1004");
            j4.setResponsibilities("Formulate statistical hypotheses, build forecasting models, and interpret massive datasets to drive key product strategy decisions.");
            j4.setRequirements("Advanced degree in Quantitative field, proficiency in SQL, Python, Pandas, statistical modeling, and A/B testing methodologies.");
            j4.setBenefits("Top-tier compensation, 401k matching / provident fund, sabbatical options, education subsidies");
            j4.setDeadline("2026-10-31");
            j4.setVacancies(2);
            jobRepository.save(j4);

            Job j5 = new Job("Frontend Web Developer", "Microsoft IDC", "Web Development", "Hyderabad, India", "Full-time", "Remote", "Fresher / 0-2 Years", "₹10,00,000 - ₹16,00,000 / year", "HTML5, CSS3, JavaScript, UI/UX Design, Bootstrap, React", "Create responsive, accessible, high-performance web user experiences across Microsoft products.", mEmail);
            j5.setJobCode("HH-JOB-1005");
            j5.setResponsibilities("Translate visual designs into clean, semantic HTML/CSS and interactive JavaScript. Ensure cross-browser compatibility and responsive performance.");
            j5.setRequirements("Expertise in modern HTML5, CSS3, Vanilla JavaScript (ES6+), DOM manipulation, responsive grid systems, and web accessibility standards.");
            j5.setBenefits("Health Coverage, Home Office Setup Reimbursement, Paid Leave, Fitness Memberships");
            j5.setDeadline("2026-12-31");
            j5.setVacancies(6);
            jobRepository.save(j5);

            Job j6 = new Job("Cyber Security Analyst", "Amazon AWS", "Cyber Security", "Bengaluru, India", "Full-time", "On-site", "1-3 Years", "₹12,00,000 - ₹18,00,000 / year", "Cyber Security, Network Security, Linux, Python, Cryptography", "Protect cloud infrastructure and detect, investigate, and remediate cybersecurity threats.", aEmail);
            j6.setJobCode("HH-JOB-1006");
            j6.setResponsibilities("Perform threat hunting, vulnerability assessments, and incident triage across mission-critical services.");
            j6.setRequirements("Understanding of TCP/IP networking, Linux administration, security auditing, SIEM tooling, and common attack vectors.");
            j6.setBenefits("Security Certifications Reimbursement, Comprehensive Health Coverage, Flexible Shifts");
            j6.setDeadline("2026-11-15");
            j6.setVacancies(3);
            jobRepository.save(j6);

            Job j7 = new Job("Mobile App Developer (Android)", "Google India", "Mobile Development", "Bengaluru, India", "Full-time", "Hybrid", "1-3 Years", "₹12,00,000 - ₹18,00,000 / year", "Java, Kotlin, Android, REST API, Git", "Build cutting-edge Android experiences that delight billions of global users.", gEmail);
            j7.setJobCode("HH-JOB-1007");
            j7.setResponsibilities("Develop native Android features, optimize app memory and battery consumption, and adhere to Material Design guidelines.");
            j7.setRequirements("Solid experience with Java/Kotlin, Android SDK, Jetpack components, background threading, and RESTful API integration.");
            j7.setBenefits("Flagship Android devices, Premium Health Cover, Stock Grant, Commute Support");
            j7.setDeadline("2026-12-31");
            j7.setVacancies(4);
            jobRepository.save(j7);

            Job j8 = new Job("QA Automation Test Engineer", "Microsoft IDC", "Testing", "Hyderabad, India", "Full-time", "Hybrid", "Fresher / 0-2 Years", "₹8,00,000 - ₹14,00,000 / year", "Java, Selenium, Testing, Test Automation, SQL, Python", "Engineer automated quality test suites to validate reliability and performance of cloud software.", mEmail);
            j8.setJobCode("HH-JOB-1008");
            j8.setResponsibilities("Develop automated end-to-end and regression test scripts. Collaborate with developers to identify edge cases and defects early.");
            j8.setRequirements("Knowledge of Selenium WebDriver, Java, test frameworks (JUnit/TestNG), SQL queries, and CI integration.");
            j8.setBenefits("Medical insurance, Flexible hours, Continuous learning courses, Team offsites");
            j8.setDeadline("2026-12-20");
            j8.setVacancies(3);
            jobRepository.save(j8);

            Job j9 = new Job("UI/UX Product Designer", "Amazon AWS", "UI/UX Design", "Remote", "Full-time", "Remote", "1-3 Years", "₹11,00,000 - ₹17,00,000 / year", "Figma, UI/UX Design, Wireframing, User Research, HTML, CSS", "Design world-class developer experiences and intuitive management consoles for AWS services.", aEmail);
            j9.setJobCode("HH-JOB-1009");
            j9.setResponsibilities("Conduct user interviews, design wireframes, high-fidelity prototypes, and establish comprehensive design systems in Figma.");
            j9.setRequirements("Portfolio showcasing web/app interface designs, user empathy, wireframing mastery in Figma, and understanding of web design fundamentals.");
            j9.setBenefits("Work from anywhere stipend, Hardware upgrade allowance, Creative tool subscriptions");
            j9.setDeadline("2026-12-31");
            j9.setVacancies(2);
            jobRepository.save(j9);

            Job j10 = new Job("Backend Microservices Engineer", "Google India", "Software Development", "Bengaluru, India", "Full-time", "On-site", "2-4 Years", "₹18,00,000 - ₹26,00,000 / year", "Java, Spring Boot, MySQL, Microservices, Docker, Kafka", "Design distributed backend systems processing millions of queries per second with low latency.", gEmail);
            j10.setJobCode("HH-JOB-1010");
            j10.setResponsibilities("Develop robust distributed systems, asynchronous event queues, database indexing strategies, and resilient microservices architectures.");
            j10.setRequirements("Strong Java backend engineering, relational database optimization, caching (Redis), and event streaming (Kafka).");
            j10.setBenefits("Google Food Courts, Onsite Gym, Global Mobility, Relocation, Competitive Stock Unit Grants");
            j10.setDeadline("2026-12-31");
            j10.setVacancies(4);
            jobRepository.save(j10);

            // --- 10+ INTERNSHIPS ---
            Job i1 = new Job("Machine Learning Research Intern", "Microsoft IDC", "Artificial Intelligence", "Remote", "Internship", "Remote", "Student / Fresher", "₹45,000 / month", "Python, Machine Learning, Deep Learning, PyTorch, SQL", "Exciting 6-month research internship applying deep learning algorithms to real-world datasets.", mEmail);
            i1.setJobCode("HH-INT-2001");
            i1.setDuration("6 Months");
            i1.setStipend("₹45,000 / month");
            i1.setResponsibilities("Work alongside Microsoft research scientists to implement baseline models, run experiments, and evaluate model accuracy.");
            i1.setRequirements("Enrolled in Computer Science, Data Science, or related undergraduate/postgraduate degree. Solid Python coding skills.");
            i1.setBenefits("Pre-Placement Offer (PPO) opportunity, 1:1 Senior Mentorship, Remote Work Setup Kit");
            i1.setDeadline("2026-12-31");
            i1.setVacancies(8);
            jobRepository.save(i1);

            Job i2 = new Job("Software Engineering Intern (Java/Spring)", "Google India", "Software Development", "Bengaluru, India", "Internship", "On-site", "Student / Fresher", "₹50,000 / month", "Java, Spring Boot, MySQL, Data Structures, Algorithms, Git", "Gain real-world industry experience working on core Google infrastructure and software systems.", gEmail);
            i2.setJobCode("HH-INT-2002");
            i2.setDuration("6 Months");
            i2.setStipend("₹50,000 / month");
            i2.setResponsibilities("Implement bug fixes, write unit tests, design feature components, and participate in code reviews with senior Google engineers.");
            i2.setRequirements("Strong command of Java, Object-Oriented Programming, Data Structures, Algorithms, and basic SQL.");
            i2.setBenefits("Certificate of Internship, High PPO Conversion Rate, Onsite Meals & Wellness Facilities");
            i2.setDeadline("2026-12-31");
            i2.setVacancies(10);
            jobRepository.save(i2);

            Job i3 = new Job("Cloud Infrastructure Intern", "Amazon AWS", "Cloud Computing", "Hyderabad, India", "Internship", "Hybrid", "Student / Fresher", "₹40,000 / month", "AWS, Linux, Docker, Python, Bash", "Hands-on cloud engineering internship working with AWS services, Linux servers, and containerization.", aEmail);
            i3.setJobCode("HH-INT-2003");
            i3.setDuration("3 Months");
            i3.setStipend("₹40,000 / month");
            i3.setResponsibilities("Automate cloud tasks using Python/Bash scripts, configure Docker containers, and monitor server health metrics.");
            i3.setRequirements("Familiarity with basic Linux terminal commands, fundamentals of cloud computing, and scripting in Python or Bash.");
            i3.setBenefits("AWS Cloud Practitioner Certification voucher, Amazon Goodies, Internship Completion Certificate");
            i3.setDeadline("2026-11-30");
            i3.setVacancies(6);
            jobRepository.save(i3);

            Job i4 = new Job("Web Development Intern", "Microsoft IDC", "Web Development", "Remote", "Internship", "Remote", "Student / Fresher", "₹30,000 / month", "HTML5, CSS3, JavaScript, Bootstrap, REST API", "Build modern responsive web pages and clean UI components for Microsoft developer portals.", mEmail);
            i4.setJobCode("HH-INT-2004");
            i4.setDuration("3 Months");
            i4.setStipend("₹30,000 / month");
            i4.setResponsibilities("Develop responsive front-end pages using HTML5, CSS3, and JavaScript. Connect web UI with backend REST APIs.");
            i4.setRequirements("Hands-on knowledge of HTML, CSS, JavaScript, responsive web layout principles, and Git version control.");
            i4.setBenefits("Flexible Working Hours, Mentorship from Senior Frontend Developers, Letter of Recommendation");
            i4.setDeadline("2026-12-15");
            i4.setVacancies(5);
            jobRepository.save(i4);

            Job i5 = new Job("Data Analytics & BI Intern", "Google India", "Data Science", "Bengaluru, India", "Internship", "Hybrid", "Student / Fresher", "₹35,000 / month", "SQL, Python, Pandas, Data Analysis, Statistics", "Extract insights from complex business datasets and create actionable visual executive dashboards.", gEmail);
            i5.setJobCode("HH-INT-2005");
            i5.setDuration("6 Months");
            i5.setStipend("₹35,000 / month");
            i5.setResponsibilities("Write advanced SQL queries, clean and organize raw datasets using Python Pandas, and build interactive dashboards.");
            i5.setRequirements("Proficiency with SQL joins, aggregations, data visualization, and analytical problem-solving.");
            i5.setBenefits("Monthly Stipend, Industry Project Experience, Certificate of Excellence");
            i5.setDeadline("2026-12-31");
            i5.setVacancies(4);
            jobRepository.save(i5);

            Job i6 = new Job("UI/UX Design Intern", "Amazon AWS", "UI/UX Design", "Remote", "Internship", "Remote", "Student / Fresher", "₹25,000 / month", "Figma, UI/UX Design, Prototyping, Wireframing, HTML, CSS", "Create user flows, wireframes, and design prototypes for consumer and cloud software products.", aEmail);
            i6.setJobCode("HH-INT-2006");
            i6.setDuration("3 Months");
            i6.setStipend("₹25,000 / month");
            i6.setResponsibilities("Assist in user research, design wireframes and interactive prototypes in Figma, and build out design component libraries.");
            i6.setRequirements("Portfolio showcasing design aptitude, knowledge of UI principles, color theory, typography, and Figma mastery.");
            i6.setBenefits("Portfolio Mentorship, Letter of Recommendation, Flexible Remote Schedule");
            i6.setDeadline("2026-12-31");
            i6.setVacancies(4);
            jobRepository.save(i6);

            Job i7 = new Job("Cyber Security Intern", "Microsoft IDC", "Cyber Security", "Hyderabad, India", "Internship", "On-site", "Student / Fresher", "₹35,000 / month", "Cyber Security, Linux, Networking, Python, Cryptography", "Learn threat analysis, network vulnerability scanning, and secure software development practices.", mEmail);
            i7.setJobCode("HH-INT-2007");
            i7.setDuration("6 Months");
            i7.setStipend("₹35,000 / month");
            i7.setResponsibilities("Assist security engineers in analyzing network logs, testing system configurations, and running vulnerability scans.");
            i7.setRequirements("Fundamental understanding of computer networks (OSI model, TCP/IP), Linux command line, and security concepts.");
            i7.setBenefits("Security Training by Experts, PPO Consideration, Certificate of Internship");
            i7.setDeadline("2026-11-30");
            i7.setVacancies(4);
            jobRepository.save(i7);

            Job i8 = new Job("Android Development Intern", "Google India", "Mobile Development", "Bengaluru, India", "Internship", "On-site", "Student / Fresher", "₹45,000 / month", "Java, Kotlin, Android, XML, REST API, Git", "Develop native Android apps and test innovative mobile features on the latest Android OS releases.", gEmail);
            i8.setJobCode("HH-INT-2008");
            i8.setDuration("6 Months");
            i8.setStipend("₹45,000 / month");
            i8.setResponsibilities("Create native Android UI layouts, implement business logic in Java/Kotlin, and consume backend REST services.");
            i8.setRequirements("Basic understanding of Android Studio, Java/Kotlin syntax, Android Activity/Fragment lifecycle, and layouts.");
            i8.setBenefits("Hands-on Mentorship by Google Android Engineers, Daily Lunch & Snacks, Internship Credential");
            i8.setDeadline("2026-12-31");
            i8.setVacancies(6);
            jobRepository.save(i8);

            Job i9 = new Job("QA & Software Testing Intern", "Amazon AWS", "Testing", "Remote", "Internship", "Remote", "Student / Fresher", "₹28,000 / month", "Java, Selenium, SQL, Testing, Manual Testing", "Learn functional, regression, and automated testing methods for mission-critical cloud web apps.", aEmail);
            i9.setJobCode("HH-INT-2009");
            i9.setDuration("3 Months");
            i9.setStipend("₹28,000 / month");
            i9.setResponsibilities("Write clear test case documentation, execute test suites, log defect tickets, and assist in automating test scripts.");
            i9.setRequirements("Good analytical mind, attention to detail, basic programming in Java or Python, and knowledge of software testing concepts.");
            i9.setBenefits("Remote Work Flexibility, Certificate of Completion, Dedicated Mentor");
            i9.setDeadline("2026-12-20");
            i9.setVacancies(5);
            jobRepository.save(i9);

            Job i10 = new Job("Full Stack Developer Intern", "Microsoft IDC", "Web Development", "Remote", "Internship", "Remote", "Student / Fresher", "₹38,000 / month", "Java, Spring Boot, HTML5, CSS3, JavaScript, MySQL", "Full stack development internship building end-to-end features with Spring Boot and web UI.", mEmail);
            i10.setJobCode("HH-INT-2010");
            i10.setDuration("6 Months");
            i10.setStipend("₹38,000 / month");
            i10.setResponsibilities("Work across full web stack: build backend APIs with Spring Boot, connect with MySQL databases, and construct responsive HTML/CSS/JS frontend views.");
            i10.setRequirements("Foundational knowledge of Java, Spring Boot, HTML, CSS, JavaScript, and relational databases.");
            i10.setBenefits("PPO Opportunity for Top Performers, Free Microsoft Cloud Subscriptions, 1-on-1 Mentorship");
            i10.setDeadline("2026-12-31");
            i10.setVacancies(7);
            jobRepository.save(i10);

            // --- SAMPLE APPLICATIONS ---
            JobApplication a1 = new JobApplication();
            a1.setJob(j1);
            a1.setApplicantEmail("alex.johnson@example.com");
            a1.setApplicantName("Alex Johnson");
            a1.setApplicantPhone("+91 9876540001");
            a1.setDegree("B.Tech in Computer Science");
            a1.setUniversity("National Institute of Technology (NIT)");
            a1.setGraduationYear(2024);
            a1.setSkills("Java, Spring Boot, MySQL, REST API, JavaScript");
            a1.setCoverLetter("I am an enthusiastic Full Stack Java developer with hands-on experience building Spring Boot applications and RESTful microservices. I would love the opportunity to contribute to Google India.");
            a1.setStatus("Shortlisted");
            a1.setMatchScore(92);
            a1.setApplicationId("HH-APP-10021");
            a1.setExpectedSalary("₹16,00,000 / year");
            a1.setAvailability("Immediate (Within 15 Days)");
            a1.setRecruiterRemarks("Strong profile and skill alignment with Spring Boot & Java backend stack. Shortlisted for Round 1 Technical Interview.");
            applicationRepository.save(a1);

            JobApplication a2 = new JobApplication();
            a2.setJob(i2);
            a2.setApplicantEmail("alex.johnson@example.com");
            a2.setApplicantName("Alex Johnson");
            a2.setApplicantPhone("+91 9876540001");
            a2.setDegree("B.Tech in Computer Science");
            a2.setUniversity("National Institute of Technology (NIT)");
            a2.setGraduationYear(2024);
            a2.setSkills("Java, Spring Boot, MySQL, Data Structures");
            a2.setCoverLetter("Applying for Software Engineering Internship. Very excited to learn from senior engineers.");
            a2.setStatus("Selected");
            a2.setMatchScore(95);
            a2.setApplicationId("HH-APP-10022");
            a2.setExpectedSalary("₹50,000 / month");
            a2.setAvailability("Immediate");
            a2.setRecruiterRemarks("Excellent performance in coding rounds. Selected for internship offer.");
            applicationRepository.save(a2);

            JobApplication a3 = new JobApplication();
            a3.setJob(i1);
            a3.setApplicantEmail("priya.sharma@example.com");
            a3.setApplicantName("Priya Sharma");
            a3.setApplicantPhone("+91 9876540002");
            a3.setDegree("M.S. in Data Science");
            a3.setUniversity("IIIT");
            a3.setGraduationYear(2023);
            a3.setSkills("Python, Machine Learning, Deep Learning, PyTorch, SQL");
            a3.setCoverLetter("I am passionate about ML research and neural network architectures. Excited to apply for the ML Research Internship.");
            a3.setStatus("Interview");
            a3.setMatchScore(94);
            a3.setApplicationId("HH-APP-10023");
            a3.setExpectedSalary("₹45,000 / month");
            a3.setAvailability("Immediate");
            a3.setRecruiterRemarks("Technical interview scheduled with Microsoft AI Research team.");
            applicationRepository.save(a3);

            JobApplication a4 = new JobApplication();
            a4.setJob(j3);
            a4.setApplicantEmail("rahul.verma@example.com");
            a4.setApplicantName("Rahul Verma");
            a4.setApplicantPhone("+91 9876540003");
            a4.setDegree("B.E. Information Technology");
            a4.setUniversity("PICT Pune");
            a4.setGraduationYear(2024);
            a4.setSkills("AWS, Docker, Kubernetes, Linux, CI/CD");
            a4.setCoverLetter("Eager to contribute my cloud automation and containerization knowledge to AWS cloud infrastructure.");
            a4.setStatus("Under Review");
            a4.setMatchScore(88);
            a4.setApplicationId("HH-APP-10024");
            a4.setExpectedSalary("₹18,00,000 / year");
            a4.setAvailability("1 Month");
            applicationRepository.save(a4);

            // Sample Saved Jobs
            savedJobRepository.save(new SavedJob("alex.johnson@example.com", j1));
            savedJobRepository.save(new SavedJob("alex.johnson@example.com", j10));
            savedJobRepository.save(new SavedJob("alex.johnson@example.com", i2));
            savedJobRepository.save(new SavedJob("candidate@hirehub.com", j1));
            savedJobRepository.save(new SavedJob("candidate@hirehub.com", i10));

            // Sample Notifications
            notificationRepository.save(new Notification("alex.johnson@example.com", "Application Shortlisted!", "Congratulations! Your application for Full Stack Java Developer at Google India has been shortlisted.", "STATUS", "/applications"));
            notificationRepository.save(new Notification("alex.johnson@example.com", "Internship Selection Offer!", "Great news! You have been selected for the Software Engineering Internship (Java/Spring) at Google India.", "APPLICATION", "/applications"));
            notificationRepository.save(new Notification("priya.sharma@example.com", "Interview Invitation", "Microsoft IDC has invited you to an interview for Machine Learning Research Intern.", "INTERVIEW", "/applications"));
            notificationRepository.save(new Notification("recruiter.google@hirehub.com", "New Candidate Application", "Alex Johnson applied for Full Stack Java Developer.", "APPLICATION", "/recruiter/applicants"));
        }
    }
}

