package com.project1.orderproject.ServiceImplementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project1.orderproject.Configuration.AppConfiguration;
import com.project1.orderproject.DTO.UserDTO;
import com.project1.orderproject.DTO.Agent.Agent_Signup_Dto;
import com.project1.orderproject.ExceptionHandler.Exception.UserAlreadyExist;
import com.project1.orderproject.POJOs.Agent;
import com.project1.orderproject.POJOs.User;
import com.project1.orderproject.POJOs.Enum.Role;
import com.project1.orderproject.Repositry.AgentRepositry;
import com.project1.orderproject.Repositry.UserRepositry;
@Service
public class UserServiceImpl implements  UserDetailsService{

    private   UserRepositry  userRepositry;
    private  AppConfiguration appConfiguration;
    @Lazy
    private  JWTService jwtService;
    @Lazy
    private  Refresh_Service refresh_Service;

 
    @Autowired
    private AgentRepositry agentRepositry;
    @Autowired
    @Lazy
    private  AuthenticationManager authenticationManager;


    
    public  UserServiceImpl(UserRepositry  userRepositry,AppConfiguration appConfiguration,JWTService jwtService,
                                Refresh_Service refresh_Service)  {
        this.userRepositry =  userRepositry;
        this.appConfiguration =  appConfiguration;
        this.jwtService =jwtService;
        this.refresh_Service =refresh_Service;
        
     
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        Optional<User>  optional =  userRepositry.findByusername(username);
       if(!optional.isPresent()) {
        throw  new UsernameNotFoundException("Hey  Buddy  Not  yet  Found");
       }
       return optional.get();
       //we  can  handle   a   Exception  here    to  solve  that  we donot  found  the   user   then  return a   good  Reponse
    }

    public  String  AddUser(UserDTO  userDTO) {
        //Hey  See  there is  a  potentail to have  the  Same  Username   Error  SO  fix this 
        Optional<User> u1  =  userRepositry.findByusername(userDTO.getUsername());
        if(!u1.isPresent()) {
            User  user  =  new  User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(userDTO.getUsername());
            user.setPassword(appConfiguration.passwordEncoder().encode(userDTO.getPassword()));
            user.setSet(new  HashSet<>(List.of(Role.Admin)));
            userRepositry.save(user);
            return jwtService.genrateJwtToken(user.getId(),  user.getSet());
        }
        else {
            throw  new   UserAlreadyExist("Username Already ecist") ;
        }   
       
        

    }
    public  String  AddUser(Agent_Signup_Dto   userDTO) {
        //Hey  See  there is  a  potentail to have  the  Same  Username   Error  SO  fix this 
        Optional<User> u1  =  userRepositry.findByusername(userDTO.getUsername());
        if(!u1.isPresent()) {
            User  user  =  new  User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(userDTO.getUsername());
            user.setPassword(appConfiguration.passwordEncoder().encode(userDTO.getPassword()));
            user.setSet(new  HashSet<>(List.of(Role.Agent)));
            userRepositry.save(user);
            return jwtService.genrateJwtToken(user.getId(),  user.getSet());
        }
        else {
            throw  new   UserAlreadyExist("Username Already ecist") ;
        }   
       
        

    }
    

    /*
     * @param UserDto  from  the   Controller  
     * @return  if  vallid user  then  return  the  Jwt  Token Out  of  that  
     *          else  return  a   API  error about  the   Login  faiied
     */
    public  Map<String,Object>  Login_Token(UserDTO u1) {
        Map<String,Object> map  = new HashMap<>();
        User user_t  = userRepositry.findByusername(u1.getUsername()).get();
        Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(u1.getUsername(), u1.getPassword() ,  user_t.getAuthorities()) );
        if (authentication.isAuthenticated()) {
            UserDetails details =  loadUserByUsername(u1.getUsername());
            String  access_token = "";
            String  refresh_token = "";
            if(details instanceof User user)  {
                access_token  =  jwtService.genrateJwtToken(user.getId(),user.getSet());
                refresh_token =  refresh_Service.genrate_refresh_User(user);
                map.put("access_Token", access_token);
                map.put("refresh_token", refresh_token);
                return  map;
            }


            
        }
        map.put("Failed", false);
        return map;
    }

    public User loadUserById(String  id) {
        Optional<User> optional   =  userRepositry.findById(id);
        if(!optional.isPresent()) {
            return null;
        }
        else {
            return  optional.get();
        }
        
    }
    public  String  genrate_jwt_userid(String  user_id) {
        Optional<User> optional   =  userRepositry.findById(user_id);
        return  jwtService.genrateJwtToken(user_id ,  optional.get().getSet());
    }

}
