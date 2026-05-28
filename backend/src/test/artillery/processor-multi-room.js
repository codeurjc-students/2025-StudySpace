const currentRunMinute = Math.floor(Date.now() / 60000);
const offsetDays = (currentRunMinute % 300) + 10; 

let targetDate = new Date();
targetDate.setDate(targetDate.getDate() + offsetDays);

//avoid weekends
if (targetDate.getDay() === 0) targetDate.setDate(targetDate.getDate() + 1);
if (targetDate.getDay() === 6) targetDate.setDate(targetDate.getDate() + 2);

const year = targetDate.getFullYear();
const month = String(targetDate.getMonth() + 1).padStart(2, '0');
const day = String(targetDate.getDate()).padStart(2, '0');

const testStartDate = `${year}-${month}-${day}T10:00:00.000+02:00`;
const testEndDate = `${year}-${month}-${day}T12:00:00.000+02:00`;

const MAX_ROOM_ID = 113; 
const MAX_CAMPUS_ID = 5; 

// random words people could search
const searchKeywords = [
  "Laboratorio", "Aula", "Mac", "Windows", "Linux", 
  "Proyector", "Reuniones", "204", "Informatica", "Eclipse" ,"Magna"
];

module.exports = {
  setupDatesAndRooms: function(context, events, done) {
    // dinamic dates
    context.vars.dynamicStartDate = testStartDate;
    context.vars.dynamicEndDate = testEndDate;
    context.vars.encodedStartDate = encodeURIComponent(testStartDate);
    context.vars.encodedEndDate = encodeURIComponent(testEndDate);

    // ramdom room for direect reservation
    context.vars.roomId = (Math.floor(Math.random() * MAX_ROOM_ID) + 1).toString();

    // random search on home page
    const randomKeyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
    const randomCampus = Math.floor(Math.random() * MAX_CAMPUS_ID) + 1;
    const randomCapacity = Math.floor(Math.random() * 30) + 10; 

    // encoded text for url
    context.vars.searchText = encodeURIComponent(randomKeyword);
    context.vars.searchCampusId = randomCampus.toString();
    context.vars.searchCapacity = randomCapacity.toString();

    // clean CSV
    if (context.vars.email) {
      context.vars.email = context.vars.email.trim();
    }
    if (context.vars.password) {
      context.vars.password = context.vars.password.trim();
    }
    
    return done();
  }
};