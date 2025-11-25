-- Multi-User Database Isolation: 
-- Application inserts are disabled because applications must be associated with authenticated users.
-- Applications can only be created via the API after user authentication.
-- Each application is automatically assigned to the authenticated user who created it.

-- Previously seeded test data (commented out for multi-user isolation):
/*
INSERT INTO application (company_name, role_name, date_of_application, job_link, tailored, job_description, referral, status)
VALUES
('Google', 'Software Engineer', '2025-01-10', 'https://careers.google.com/jobs/results/123456', true, 'Fresher role, must know Java, C++ or Python.', false, 'APPLIED'),
('Amazon', 'Backend Developer', '2025-01-15', 'https://amazon.jobs/en/jobs/78910', false, 'Experience in Spring Boot and AWS preferred.', true, 'APPLIED'),
('Microsoft', 'Cloud Engineer', '2025-01-20', 'https://careers.microsoft.com/us/en/job/456789', true, 'Work on Azure cloud services. Requires C# or Java.', false, 'REJECTED'),
('Netflix', 'Full Stack Developer', '2025-01-25', 'https://jobs.netflix.com/apply/654321', false, 'React + Node.js stack. Strong design sense.', false, 'REJECTED'),
('Tesla', 'Data Engineer', '2025-02-01', 'https://www.tesla.com/careers/job/112233', true, 'Build pipelines with Kafka, Spark, and SQL.', true, 'OFFER'),
('Apple', 'iOS Developer', '2025-02-05', 'https://jobs.apple.com/en-us/details/678901', true, 'Swift, SwiftUI, and UIKit experience.', false, 'APPLIED'),
('Meta', 'ML Engineer', '2025-02-10', 'https://www.metacareers.com/jobs/223344', true, 'Work on LLM training pipelines and PyTorch.', true, 'INTERVIEW'),
('Airbnb', 'Frontend Developer', '2025-02-12', 'https://careers.airbnb.com/positions/778899', false, 'React, TypeScript, and GraphQL expertise.', false, 'APPLIED'),
('Adobe', 'Software Engineer Intern', '2025-02-14', 'https://adobe.wd5.myworkdayjobs.com/en-US/external_experienced/job/998877', true, 'Java, REST APIs, and data structures.', false, 'APPLIED'),
('Stripe', 'Backend Engineer', '2025-02-17', 'https://stripe.com/jobs/123987', true, 'Work with Java and distributed systems.', true, 'INTERVIEW'),
('NVIDIA', 'AI Research Intern', '2025-02-20', 'https://nvidia.wd5.myworkdayjobs.com/en-US/nvidia/job/334455', true, 'CUDA, Python, Deep Learning frameworks.', false, 'APPLIED'),
('Uber', 'SDE I', '2025-02-22', 'https://www.uber.com/us/en/careers/list/888111', false, 'Build scalable backend systems using Go.', true, 'INTERVIEW'),
('Lyft', 'Backend Engineer', '2025-02-23', 'https://www.lyft.com/careers/333666', true, 'Work on APIs and services in Python & Flask.', false, 'APPLIED'),
('DoorDash', 'Data Analyst', '2025-02-25', 'https://careers.doordash.com/jobs/555999', false, 'SQL, PowerBI, and data storytelling.', false, 'APPLIED'),
('Robinhood', 'Full Stack Engineer', '2025-02-28', 'https://robinhood.com/careers/112299', true, 'React, Django, and PostgreSQL experience.', true, 'REJECTED'),
('Intuit', 'Software Engineer', '2025-03-02', 'https://jobs.intuit.com/details/667788', true, 'Node.js, Express, and GraphQL.', false, 'APPLIED'),
('Salesforce', 'Platform Developer', '2025-03-05', 'https://salesforce.wd1.myworkdayjobs.com/External_Career_Site/job/889900', false, 'Apex, Lightning, and Salesforce SDK.', true, 'APPLIED'),
('Oracle', 'Java Developer', '2025-03-08', 'https://careers.oracle.com/jobs/334400', true, 'Spring Boot, JDBC, and Oracle SQL.', false, 'APPLIED'),
('PayPal', 'Backend Engineer', '2025-03-10', 'https://paypal.eightfold.ai/careers/job/445566', false, 'Java, RESTful APIs, and microservices.', true, 'APPLIED'),
('LinkedIn', 'SWE Intern', '2025-03-15', 'https://careers.linkedin.com/jobs/990011', true, 'Work on distributed systems and Java.', false, 'REJECTED'),
('Dropbox', 'Infrastructure Engineer', '2025-03-18', 'https://www.dropbox.com/jobs/listing/444222', true, 'Kubernetes, Terraform, and GCP.', true, 'APPLIED'),
('Atlassian', 'DevOps Engineer', '2025-03-20', 'https://www.atlassian.com/company/careers/776655', true, 'CI/CD pipelines, Docker, and AWS.', false, 'INTERVIEW'),
('Spotify', 'Backend Developer', '2025-03-22', 'https://www.spotifyjobs.com/jobs/443322', false, 'Microservices, Kotlin, and event-driven design.', true, 'OFFER'),
('TikTok', 'Software Engineer', '2025-03-25', 'https://careers.tiktok.com/position/998100', true, 'Python, backend logic, and content moderation systems.', false, 'APPLIED'),
('Snap', 'Android Developer', '2025-03-28', 'https://careers.snap.com/jobs/234567', false, 'Kotlin and Android Jetpack.', true, 'APPLIED'),
('Pinterest', 'ML Engineer', '2025-04-01', 'https://www.pinterestcareers.com/job/889100', true, 'Recommendation systems using TensorFlow.', false, 'INTERVIEW'),
('Reddit', 'Full Stack Engineer', '2025-04-03', 'https://www.redditinc.com/careers/778800', false, 'React, Flask, and Redis.', true, 'APPLIED'),
('Shopify', 'Software Developer', '2025-04-05', 'https://www.shopify.com/careers/556677', true, 'Ruby on Rails and GraphQL APIs.', false, 'REJECTED'),
('Zoom', 'Cloud Engineer', '2025-04-08', 'https://explore.zoom.us/en/careers/job/101112', false, 'AWS, Docker, and monitoring systems.', true, 'APPLIED'),
('Twilio', 'Backend Developer', '2025-04-10', 'https://www.twilio.com/company/jobs/787878', true, 'Messaging APIs and scalable backend design.', false, 'INTERVIEW'),
('Databricks', 'Data Engineer', '2025-04-13', 'https://www.databricks.com/company/careers/454545', false, 'Spark, ETL, and Delta Lake.', true, 'APPLIED'),
('Palantir', 'Forward Deployed Engineer', '2025-04-15', 'https://www.palantir.com/careers/334466', true, 'Data integration, SQL, and distributed systems.', false, 'INTERVIEW'),
('Asana', 'Backend Engineer', '2025-04-18', 'https://asana.com/jobs/223355', false, 'Python, GraphQL, and system reliability.', false, 'APPLIED'),
('Notion', 'Full Stack Developer', '2025-04-20', 'https://www.notion.so/careers/998877', true, 'React, Next.js, and API design.', true, 'APPLIED'),
('Figma', 'Frontend Engineer', '2025-04-22', 'https://www.figma.com/careers/667788', false, 'Work on collaborative design tools with React.', false, 'INTERVIEW'),
('Stripe', 'SRE', '2025-04-24', 'https://stripe.com/jobs/998766', true, 'Kubernetes, AWS, and observability tools.', true, 'APPLIED'),
('Snowflake', 'Data Platform Engineer', '2025-04-26', 'https://careers.snowflake.com/job/334499', false, 'SQL optimization and cloud integration.', false, 'REJECTED'),
('Cohere', 'NLP Engineer', '2025-04-28', 'https://cohere.ai/careers/445577', true, 'Transformers, LLM fine-tuning, and LangChain.', true, 'INTERVIEW'),
('Anthropic', 'AI Research Engineer', '2025-04-30', 'https://www.anthropic.com/careers/112233', true, 'LLM safety and reasoning systems.', false, 'OFFER'),
('OpenAI', 'Applied Engineer', '2025-05-02', 'https://openai.com/careers/778899', false, 'Integrate GPT APIs and optimize inference performance.', true, 'INTERVIEW'),
('DeepMind', 'Research Engineer', '2025-05-05', 'https://deepmind.com/careers/job/221144', true, 'Reinforcement learning and ML ops.', false, 'APPLIED'),
('Adobe', 'Backend Developer', '2025-05-08', 'https://adobe.wd5.myworkdayjobs.com/en-US/external_experienced/job/334400', false, 'Java, Spring Boot, and REST APIs.', true, 'APPLIED'),
('ZoomInfo', 'Data Analyst', '2025-05-10', 'https://www.zoominfo.com/careers/776600', true, 'Data pipelines, SQL, and visualization.', false, 'APPLIED'),
('GitHub', 'Developer Advocate', '2025-05-12', 'https://github.com/about/careers/443322', true, 'Promote GitHub Actions and community contributions.', false, 'APPLIED'),
('Vercel', 'Full Stack Engineer', '2025-05-14', 'https://vercel.com/careers/556677', true, 'Next.js, serverless APIs, and edge functions.', true, 'APPLIED'),
('Replit', 'Platform Engineer', '2025-05-16', 'https://replit.com/careers/221155', false, 'Container orchestration and performance optimization.', false, 'REJECTED'),
('GitLab', 'DevOps Engineer', '2025-05-18', 'https://about.gitlab.com/jobs/889977', true, 'CI/CD automation and Docker infrastructure.', false, 'APPLIED'),
('Coursera', 'Software Engineer', '2025-05-20', 'https://about.coursera.org/careers/334488', true, 'Work on education platform backend using Java.', false, 'APPLIED'),
('Duolingo', 'Frontend Developer', '2025-05-22', 'https://careers.duolingo.com/jobs/998866', false, 'React, UI/UX optimization, and localization.', true, 'APPLIED');
*/
