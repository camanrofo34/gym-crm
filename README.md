<h1>🏋️‍♂️ Gym Management Application - EPAM Specialization Project</h1>

<h2>📄 Overview</h2>
<p>
  This project is part of the <strong>[Specialization] Java, Part 1. Classic, LatAm #13</strong> course by EPAM.
  The main objective of this task is to build a <strong>Gym Management Application</strong> that allows gym members (trainees) and trainers
  to manage their profiles and activities effectively. The system ensures proper handling of user interactions, activity tracking,
  and profile management, all with a secure login process.
</p>

<h2>🎯 Features</h2>
<ul>
  <li><strong>User Registration</strong>
    <ul>
      <li>Both <strong>trainees</strong> and <strong>trainers</strong> can register their profiles.</li>
      <li>Trainees have the ability to select one or more trainers during registration or profile update.</li>
    </ul>
  </li>
  <li><strong>Authentication</strong>
    <ul>
      <li>Login credentials are mandatory to access any section of the application except the registration page.</li>
      <li>Separate access for trainees and trainers.</li>
    </ul>
  </li>
  <li><strong>Profile Management</strong>
    <ul>
      <li>Modify profile information.</li>
      <li>Activate or deactivate user profiles (both trainees and trainers).</li>
    </ul>
  </li>
  <li><strong>Activity Logging</strong>
    <ul>
      <li>Users (trainees and trainers) can log their gym activities.</li>
      <li>Activities can be reviewed both from the trainee and trainer perspectives.</li>
    </ul>
  </li>
  <li><strong>Trainer Performance Metrics</strong>
    <ul>
      <li>For each trainer, the system calculates the total duration of their trainings on a <strong>weekly basis</strong>.</li>
    </ul>
  </li>
</ul>

<h2>🏗️ Technologies Used</h2>
<ul>
  <li><strong>Language:</strong> Java</li>
  <li><strong>Framework:</strong> Spring Boot </li>
  <li><strong>Build Tool:</strong> Maven </li>
  <li><strong>Testing:</strong> JUnit, Mockito</li>
  <li><strong>Version Control:</strong> Git</li>
</ul>

<h2>🚀 How to Run</h2>
<ol>
  <li>Clone the repository:
    <pre><code>git clone https://github.com/camanrofo34/gym-crm.git</code></pre>
  </li>
  <li>Build the project:
    <pre><code>mvn clean install</code></pre>
  </li>
  <li>Run the application:
    <pre><code>java -jar target/gym-crm.jar</code></pre>
  </li>
</ol>

<h2>📝 Future Improvements</h2>
<ul>
  <li>Implementation of REST endpoints to call the methods.</li>
  <li>Implementation of a front-end interface (web or mobile).</li>
  <li>Advanced reporting for trainer and trainee activity analytics.</li>
  <li>Notification system for scheduled trainings.</li>
  <li>Role-based permissions for enhanced security.</li>
</ul>

<h2>📌 Notes</h2>
<p>
  This project is developed as an academic exercise and focuses on applying Java fundamentals to build a real-world simulation.
  Further enhancements and refinements may be applied as part of future iterations or additional modules in the specialization.
</p>

<hr/>


