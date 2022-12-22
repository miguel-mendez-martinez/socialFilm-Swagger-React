let __instance = null

export default class API {
    #token = sessionStorage.getItem('token') || null

    static instance() {
        if(__instance == null)
            __instance = new API()

        return __instance
    }

    async login(email, pass) {
        // TODO fetch from API and if successful, store token from response headers

        const login = await fetch(`http://localhost:8081/login`,{
            method: "POST",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded'},
            body: JSON.stringify({ email: email, password: pass }) 
        })

        if(login.status === 200){
            //se ha hecho el login correctamente
            localStorage.setItem('user', email)
            localStorage.setItem('token', login.headers.get("authentication"))
            this.#token = login.headers.get("authentication")
            return true
        }else{
            return false
        }
    }
    async logout() {
        this.#token = null
        localStorage.clear()

        return true
    }
    async findMovies(filter) {
        //hay que construir el filter, basicamente ver que tiene
        let url = "http://localhost:8081/films?"
        for (let key in filter){
            for(let key2 in filter[key]){
                if(filter[key][key2] === ""){
                    continue
                }else if(key === "sort"){
                    //el srot es un caso especial, en el que podríamos tener varios parametros, ademas hay que traducir ASC y DESC a a y -
                    url = url + `sort=`
                    if(filter[key][key2] === "ASC"){
                        url = url + `a${key2}&`
                    }else if(filter[key][key2] === "DESC"){
                        url = url + `-${key2}&`
                    }else{
                        //se ha puesto mal el sort asi que va sin ordenar
                        console.log("error en el sort")
                    }
                }
                else{
                    url = url + `${key2}=${filter[key][key2]}&`
                }
            }   
            //para todos los filtros que se pongan se añade a la url
            //para sort, tendremos que ver si es ASC o DESC y añadir todos los sort que se hayan puesto
        }
        if(url === "http://localhost:8081/films?"){
            //no hubo filtros
            url = "http://localhost:8081/films/"
        }else{
            //hay que quitar el & final
            url = url.slice(0, -1)
        }
        const films = await fetch(url,{
            method: "GET",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 
                        'Authorization': localStorage.getItem('token')}})
        return await films.json()
        
        
    }

    async findMovie(id) {

        let url = `http://localhost:8081/films/${id}`
        const films = await fetch(url,{
            method: "GET",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 
                        'Authorization': localStorage.getItem('token')}})
        return await films.json()
    }

    async updateFilm(id, changes){
        //se llama al patch
        let url = `http://localhost:8081/films/${id}`
        const films = await fetch(url,{
            method: "PATCH",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')},
            body: JSON.stringify(changes)})
                      
            
        return await films.json()
    }

    async findUser(id) {
        if(id === null){
            return null
        }

        const user = await fetch(`http://localhost:8081/users/${id}`,{
            method: "GET",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 
                        'Authorization': localStorage.getItem('token')}})
        return await user.json()
    }

    async searchUser(id) {
        if(id === null){
            return null
        }

        const user = await fetch(`http://localhost:8081/users?email=${id}`,{
            method: "GET",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 
                        'Authorization': localStorage.getItem('token')}})
        return await user.json()
    }

    async findComments(filter) {
        let url = "http://localhost:8081/assessements?"
        for (let key in filter){
            for(let key2 in filter[key]){
                if(filter[key][key2] === ""){
                    continue
                }else{
                    url = url + `${key2}=${filter[key][key2]}&`
                }
            }
        }
        if(url === "http://localhost:8081/assessements?"){
            //no hubo filtros
            url = "http://localhost:8081/assessements/"
        }else{
            //hay que quitar el & final
            url = url.slice(0, -1)
        }

        const coments = await fetch(url,{
            method: "GET",
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 
                        'Authorization': localStorage.getItem('token')}})
        return await coments.json()
        
    }

    async createComment(comment) {
        let url = "http://localhost:8081/assessements/"

        return await fetch(url,{
            method: "POST",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')}, 
            body: JSON.stringify(comment) })
        //return await nuevoComent.json()
    }

    async createUser(user) {
        let url = "http://localhost:8081/users/"
        return await fetch(url,{
            method: "POST",
            headers: { 'Content-Type': 'application/json'}, 
            body: JSON.stringify(user) })
    }

    async updateUser(id, changes) {
        let url = `http://localhost:8081/users/${id}`
        const films = await fetch(url,{
            method: "PATCH",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')},
            body: JSON.stringify(changes)})
                      
            
        return await films.json()
    }

    async searchFriends(user) {
        let url = `http://localhost:8081/users/${user}/friends/`
        const friends = await fetch(url,{
            method: "GET",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')}})
        let finalFriends = await friends.json()
        return await finalFriends
    }

    async areFriends(user, friend){
        let url = `http://localhost:8081/users/${user}/friends?user=${friend}`
        const friends = await fetch(url,{
            method: "GET",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')}})
        return await friends.json()
    }

    async pedirAmistad(peticion) {
        let url = `http://localhost:8081/users/${peticion.user}/friends/`
        const newFriend = await fetch(url,{
            method: "POST",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')},
            body: JSON.stringify(peticion)})
                      
        return await newFriend.json()
    }

    async confirmarAmistad(id, user){
        let confirmacion = [
            {
                op: "replace",
                path: "/confirmed",
                value: true
            }
        ]
        let url = `http://localhost:8081/users/${user}/friends/${id}`
        const films = await fetch(url,{
            method: "PATCH",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')},
            body: JSON.stringify(confirmacion)})  
        return await films.json()
    }

    async eliminarAmistad(id, user) {
        let url = `http://localhost:8081/users/${user}/friends/${id}`
        const deleteFriend = await fetch(url,{
            method: "DELETE",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')}})
                      
        return await deleteFriend.json()
    }

    async createFilm(movie) {
        console.log(movie)
        let url = `http://localhost:8081/films/`
        const newFilm = await fetch(url,{
            method: "POST",
            headers: { 'Content-Type': 'application/json', 
                        'Authorization': localStorage.getItem('token')},
            body: JSON.stringify(movie)})
                      
        return await newFilm.json()
    }
} 