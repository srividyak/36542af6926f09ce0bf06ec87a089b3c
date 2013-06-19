<html>
  <body>
    <form method="post" action="">
      <label for="firstName">firstName:</label><input id="firstName" name="firstName" type="text"/><br/>
      <label for="lastName">lastname:</label><input id="lastName" name="lastName" type="text"/><br/>
      <label for="middleName">middlename:</label><input id="middleName" name="middleName" type="text"/><br/>
      <label for="dob">dob:</label><input id="dob" name="dob" type="text"/><br/>
      <label for="interests">interests:</label><input id="interests" name="interests" type="text"/><br/>
      <label for="phoneNum">phoneNum:</label><input id="phoneNum" name="phoneNum" type="text"/><br/>
      <label for="about">about:</label><input id="about" name="about" type="text"/><br/>
      <label for="email">email:</label><input id="email" name="email" type="text"/><br/>
      <select id="gender" name="gender">
        <options>
          <option value=false>female</option>
          <option value=true>male</option>
        </options>
      </select><br/>

      <select id="relStatus" name="relStatus">
        <options>
          <option value='1'>single</option>
          <option value='2'>in a relationship</option>
          <option value='3'>engaged</option>
          <option value='4'>married</option>
          <option value='5'>it's complicated</option>
          <option value='6'>in an open relationship</option>
          <option value='7'>widowed</option>
          <option value='8'>separated</option>
          <option value='9'>divorced</option>
        </options>
      </select>
      <input type="submit" value="submit"/>
    </form>
    <label for="userSearch"></label><input type="text" id="userSearch" name="userSearch"/>
  </body>
  <script src="http://yui.yahooapis.com/3.9.1/build/yui/yui-min.js"></script>
  <script src="http://localhost/js/userSearchIO.js"></script>
</html>
