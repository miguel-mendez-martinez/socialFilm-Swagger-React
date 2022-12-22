
import { ArrowCircleLeftOutline as Back, CheckCircleOutline as Check } from '@graywolfai/react-heroicons'
import { useState, useEffect } from 'react'

import { Shell, Link } from '../../components'

import {useUser } from '../../hooks'

const backdrop = user => {
    const backdrop = user?.picture === 'none' ? "https://media.istockphoto.com/id/517998264/es/vector/hombre-icono-de-usuario.jpg?s=612x612&w=is&k=20&c=qX2mEhSna0SxcxMXXyS8YZRaYBm8itZ9N9Ma1S4Bug4=" : user.picture

    return backdrop
}

export default function ProfileEdit() {
    const { user, create, update } = useUser(localStorage.getItem('email'))

    return <Shell>
        <img style = {{ height: '28rem' }}
            src = { backdrop(user) }
            alt = { `${user} backdrop` }
            className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <Link variant = 'primary'
            className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
            to = '/'
        >
            <Back className = 'w-8 h-8'/>
            <span>Volver</span>
        </Link>
        
        <Link variant = 'primary'
              className = 'rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
              to = {`/profile/${user.email}`}>
            <Check className = 'w-8 h-8'/>
            <h1>Done</h1>
        </Link>
        

        <div className = 'mx-auto max-w-screen-2xl p-8'>
            <Header user = { user } update = {update}/>
        </div>
    </Shell>
}

function Header({ user, update }) {
    const [userName, setUserName] = useState('')
    const [userLocation, setUserLocation] = useState('')
    const [userPicture, setUserPicture] = useState('')

    useEffect(() => {
        setUserName(user.name);
        setUserLocation(user.country)
        setUserPicture(user.picture)
    }, [user.name])

    const handleName = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        if(userName === ""){
            setUserName(user.name)
        }else{
            let changes = [{
                op: "replace",
                path: "/name",
                value: userName
            }]
            update(user.email, changes)
        }
    }

    const handleLocation = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        if(userLocation === ""){
            setUserLocation(user.country)
        }else{
            let changes = [{
                op: "replace",
                path: "/country",
                value: userLocation
            }]
            update(user.email, changes)
        }
    }

    const handleUserPicture = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        if(userPicture === ""){
            setUserPicture('none')
            let changes = [{
                op: "replace",
                path: "/picture",
                value: 'none'
            }]
            update(user.email, changes)
        }else{
            let changes = [{
                op: "replace",
                path: "/picture",
                value: userPicture
            }]
            update(user.email, changes)
        }
    }

    return <header className = 'mt-28 min-h-screen relative flex items-center pb-8 mb-8 '>
        {user.length === 0 ?  null : <>
        <div className='flex flex-col'>
            <img style = {{ height: "250px" }}
             src = { backdrop(user) }
             alt = { `${ user?.email } poster` }
             className = 'w-64 rounded-full z-20' />
             <div className='flex items-center'>
                <input className='mt-2 p-2 border' value={userPicture} onChange={(e) => setUserPicture(e.target.value)}></input>
                <Check className = 'w-12 h-12 ml-2 mt-2 hover:bg-grey-700' onClick={handleUserPicture} style={{cursor: 'pointer'}}/>
             </div>
        </div>
        <hgroup className = 'flex-1'>
        <div className='bg-black bg-opacity-50 backdrop-filter backdrop-blur flex justify-end items-center'>
                <input className = {`w-full bg-black bg-opacity-0 backdrop-filter backdrop-blur text-right text-white text-6xl font-bold p-8`}
                        value = { userName }
                        onChange = {(e) => setUserName(e.target.value)}/>
                <Check className = 'w-16 h-16 text-white hover:bg-grey-700' onClick={handleName} style={{cursor: 'pointer'}}/>
            </div>
            <div className='p-8 flex justify-center space-x-44'>
                <span className='flex'>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.birthday.day}/{user.birthday.month}/{user.birthday.year}  </h1>
                </span>

                <span className='flex'>
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
                    </svg>
                    <input className={`w-44 text-black text-4xl font-boldp-8`} value={userLocation} onChange = {(e) => setUserLocation(e.target.value)}/>
                    <Check className = 'w-10 h-10 ml-2 hover:bg-grey-700' onClick={handleLocation} style={{cursor: 'pointer'}}/>
                </span>

                <span className='flex'>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.email}  </h1>
                </span>
            </div>
        </hgroup> </>}
    </header>
}