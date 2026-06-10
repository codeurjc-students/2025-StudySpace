const validRooms = [];
for (let i = 1; i <= 113; i++) {
  if (i !== 11 && i !== 12) {
    validRooms.push(i);
  }
}
const MAX_CAMPUS_ID = 5; 
const MAX_USERS_IN_DB = 100; 

const searchKeywords = [
  "Laboratorio", "Aula", "Mac", "Windows", "Linux", 
  "Proyector", "Reuniones", "204", "Informatica", "Eclipse" ,"Magna"
];

module.exports = {
  setupDatesAndRooms: function(context, events, done) {
    const userNumber = Math.floor(Math.random() * MAX_USERS_IN_DB) + 1;
    context.vars.email = `testuser${userNumber}@urjc.es`;
    context.vars.password = "1234aA.."; 

    const roomIndex = Math.floor(Math.random() * validRooms.length);
    context.vars.roomId = validRooms[roomIndex].toString();

    const offsetDays = Math.floor(Math.random() * 15) + 1; 
    
    let targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + offsetDays);

    if (targetDate.getDay() === 0) targetDate.setDate(targetDate.getDate() + 1);
    if (targetDate.getDay() === 6) targetDate.setDate(targetDate.getDate() + 2);

    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const day = String(targetDate.getDate()).padStart(2, '0');

    const startHour = Math.floor(Math.random() * 4) + 10; 
    const endHour = startHour + 1; 

    const strStartHour = String(startHour).padStart(2, '0');
    const strEndHour = String(endHour).padStart(2, '0');

    const testStartDate = `${year}-${month}-${day}T${strStartHour}:00:00.000Z`;
    const testEndDate = `${year}-${month}-${day}T${strEndHour}:00:00.000Z`;

    context.vars.dynamicStartDate = testStartDate;
    context.vars.dynamicEndDate = testEndDate;
    context.vars.encodedStartDate = encodeURIComponent(testStartDate);
    context.vars.encodedEndDate = encodeURIComponent(testEndDate);

    const randomKeyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
    const randomCampus = Math.floor(Math.random() * MAX_CAMPUS_ID) + 1;
    
    const randomCapacity = Math.floor(Math.random() * 30) + 10; 

    context.vars.searchText = encodeURIComponent(randomKeyword);
    context.vars.searchCampusId = randomCampus.toString();
    context.vars.searchCapacity = randomCapacity.toString();
    
    return done();
  }
};